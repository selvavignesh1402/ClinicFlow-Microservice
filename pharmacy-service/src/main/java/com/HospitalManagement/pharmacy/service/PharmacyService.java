package com.HospitalManagement.pharmacy.service;

import com.HospitalManagement.pharmacy.client.AuthClient;
import com.HospitalManagement.pharmacy.client.ClinicalClient;
import com.HospitalManagement.pharmacy.client.PatientClient;
import com.HospitalManagement.pharmacy.entity.DispenseRecord;
import com.HospitalManagement.pharmacy.entity.InventoryItem;
import com.HospitalManagement.shared.dto.PatientSummaryDto;
import com.HospitalManagement.shared.dto.PrescriptionSummaryDto;
import com.HospitalManagement.shared.dto.UserSummaryDto;
import com.HospitalManagement.shared.enums.DispenseStatus;
import com.HospitalManagement.shared.enums.InventoryStatus;
import com.HospitalManagement.shared.enums.MedicationStatus;
import com.HospitalManagement.shared.enums.PrescriptionStatus;
import com.HospitalManagement.pharmacy.repository.DispenseRecordRepository;
import com.HospitalManagement.pharmacy.repository.InventoryItemRepository;
import com.HospitalManagement.pharmacy.dto.PharmacyRequestDto;
import com.HospitalManagement.pharmacy.dto.DispenseResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class PharmacyService {

    private static final Logger logger = LoggerFactory.getLogger(PharmacyService.class);

    private final DispenseRecordRepository dispenseRepository;
    private final InventoryItemRepository inventoryRepository;
    private final InventoryService inventoryService;
    private final AuthClient authClient;
    private final PatientClient patientClient;
    private final ClinicalClient clinicalClient;

    public PharmacyService(
            DispenseRecordRepository dispenseRepository,
            InventoryItemRepository inventoryRepository,
            InventoryService inventoryService,
            AuthClient authClient,
            PatientClient patientClient,
            ClinicalClient clinicalClient
    ) {
        this.dispenseRepository = dispenseRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryService = inventoryService;
        this.authClient = authClient;
        this.patientClient = patientClient;
        this.clinicalClient = clinicalClient;
    }

    @Transactional(readOnly = true)
    public List<DispenseResponseDto> getAllDispenseRecords() {
        logger.debug("Fetching all dispense records");
        List<DispenseResponseDto> records = toDtos(dispenseRepository.findAll());
        logger.info("Retrieved {} dispense records", records.size());
        return records;
    }

    @Transactional(readOnly = true)
    public DispenseResponseDto getDispenseRecordById(Long dispenseId) {
        logger.debug("Fetching dispense record by ID: {}", dispenseId);
        DispenseResponseDto record = toDto(findDispenseRecord(dispenseId));
        logger.info("Retrieved dispense record - ID: {}, Quantity: {}", dispenseId, record.quantity());
        return record;
    }

    @Transactional(readOnly = true)
    public List<DispenseResponseDto> getDispenseRecordsByPrescription(Long prescriptionId) {
        logger.debug("Fetching dispense records for prescription ID: {}", prescriptionId);
        List<DispenseResponseDto> records = toDtos(dispenseRepository.findByRxId(prescriptionId));
        logger.info("Retrieved {} dispense records for prescription ID: {}", records.size(), prescriptionId);
        return records;
    }

    public DispenseResponseDto dispensePrescription(PharmacyRequestDto request) {
        logger.info("Dispensing prescription - Prescription ID: {}, Quantity: {}", request.prescriptionId(), request.quantity());
        PrescriptionSummaryDto prescription = findPrescription(request.prescriptionId());
        UserSummaryDto dispensedBy = findUser(request.dispensedById());
        InventoryItem item = request.inventoryItemId() == null
                ? findAvailableBatch(prescription, request.quantity())
                : inventoryService.findInventoryItem(request.inventoryItemId());

        validateDispense(prescription, item, request.quantity());

        item.setQuantity(item.getQuantity() - request.quantity());
        if (item.getQuantity() == 0) {
            item.setStatus(InventoryStatus.OUT_OF_STOCK);
        }
        inventoryRepository.save(item);

        DispenseRecord record = new DispenseRecord();
        record.setRxId(prescription.getRxId());
        record.setInventoryItem(item);
        record.setPatientId(prescription.getPatientId());
        record.setDispensedById(dispensedBy.getUserId());
        record.setQuantity(request.quantity());
        record.setDispensedAt(LocalDateTime.now());
        record.setNotes(request.notes());
        record.setStatus(DispenseStatus.DISPENSED);

        // Update prescription status via Feign
        clinicalClient.updatePrescriptionStatus(prescription.getRxId(), PrescriptionStatus.DISPENSED);

        DispenseRecord savedRecord = dispenseRepository.save(record);
        logger.info("Successfully dispensed prescription - Dispense ID: {}, Prescription ID: {}", savedRecord.getDispenseId(), request.prescriptionId());
        return toDto(savedRecord);
    }

    public DispenseResponseDto returnDispense(Long dispenseId, PharmacyRequestDto request) {
        logger.info("Processing dispense return - Dispense ID: {}, Return Quantity: {}", dispenseId, request.quantity());
        DispenseRecord record = findDispenseRecord(dispenseId);
        if (record.getStatus() != DispenseStatus.DISPENSED) {
            logger.warn("Attempted return of non-dispensed record - ID: {}, Status: {}", dispenseId, record.getStatus());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only dispensed records can be returned");
        }
        if (request.quantity() > record.getQuantity()) {
            logger.warn("Return quantity exceeds dispensed quantity - Dispense ID: {}, Requested: {}, Dispensed: {}", dispenseId, request.quantity(), record.getQuantity());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Return quantity is too high");
        }

        InventoryItem item = record.getInventoryItem();
        item.setQuantity(item.getQuantity() + request.quantity());
        if (item.getStatus() == InventoryStatus.OUT_OF_STOCK) {
            item.setStatus(InventoryStatus.IN_STOCK);
        }
        inventoryRepository.save(item);

        record.setStatus(request.quantity().equals(record.getQuantity()) ? DispenseStatus.RETURNED : DispenseStatus.PARTIALLY_RETURNED);
        DispenseRecord updatedRecord = dispenseRepository.save(record);
        logger.info("Successfully processed dispense return - Dispense ID: {}, Status: {}", dispenseId, updatedRecord.getStatus());
        return toDto(updatedRecord);
    }

    private void validateDispense(PrescriptionSummaryDto prescription, InventoryItem item, Integer quantity) {
        logger.debug("Validating dispense - Prescription ID: {}, Item ID: {}, Quantity: {}", prescription.getRxId(), item.getInventoryId(), quantity);
        if (prescription.getStatus() != PrescriptionStatus.ISSUED) {
            logger.warn("Prescription not in issued status - ID: {}, Status: {}", prescription.getRxId(), prescription.getStatus());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Prescription is not issued");
        }
        if (quantity > prescription.getQuantity()) {
            logger.warn("Dispense quantity exceeds prescription quantity - Requested: {}, Prescribed: {}", quantity, prescription.getQuantity());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity is more than prescription quantity");
        }
        if (!item.getMedication().getMedId().equals(prescription.getMedicationId())) {
            logger.warn("Medication mismatch - Prescription Med ID: {}, Inventory Med ID: {}", prescription.getMedicationId(), item.getMedication().getMedId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Medicine does not match prescription");
        }
        if (!inventoryService.isDispensableBatch(item) || item.getQuantity() < quantity) {
            logger.warn("Insufficient stock - Item ID: {}, Available: {}, Requested: {}", item.getInventoryId(), item.getQuantity(), quantity);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock is not available");
        }
        if (item.getMedication().getStatus() != MedicationStatus.ACTIVE) {
            logger.warn("Inactive medication - Med ID: {}, Status: {}", item.getMedication().getMedId(), item.getMedication().getStatus());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Medication is inactive"
            );
        }
        logger.debug("Dispense validation passed");
    }

    private InventoryItem findAvailableBatch(PrescriptionSummaryDto prescription, Integer quantity) {
        logger.debug("Finding available batch for prescription - Med ID: {}, Quantity: {}", prescription.getMedicationId(), quantity);
        InventoryItem selected = null;
        for (InventoryItem item : inventoryRepository.findByMedicationMedId(prescription.getMedicationId())) {
            if (inventoryService.isDispensableBatch(item) && item.getQuantity() >= quantity) {
                if (selected == null || item.getExpiryDate().isBefore(selected.getExpiryDate())) {
                    selected = item;
                }
            }
        }
        if (selected == null) {
            logger.warn("No available batch found for medication - Med ID: {}, Quantity: {}", prescription.getMedicationId(), quantity);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No stock available");
        }
        logger.debug("Selected batch for dispense - Item ID: {}, Expiry: {}", selected.getInventoryId(), selected.getExpiryDate());
        return selected;
    }

    private PrescriptionSummaryDto findPrescription(Long prescriptionId) {
        logger.debug("Looking up prescription by ID: {}", prescriptionId);
        try {
            PrescriptionSummaryDto prescription = clinicalClient.getPrescriptionById(prescriptionId);
            logger.debug("Found prescription - ID: {}, Status: {}", prescriptionId, prescription.getStatus());
            return prescription;
        } catch (Exception e) {
            logger.error("Prescription not found: {}", prescriptionId, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Prescription not found");
        }
    }

    private UserSummaryDto findUser(Long userId) {
        logger.debug("Looking up user by ID: {}", userId);
        try {
            UserSummaryDto user = authClient.getUserById(userId);
            logger.debug("Found user - ID: {}, Username: {}", userId, user.getEmail());
            return user;
        } catch (Exception e) {
            logger.error("User not found: {}", userId, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }

    private DispenseRecord findDispenseRecord(Long dispenseId) {
        logger.debug("Looking up dispense record by ID: {}", dispenseId);
        DispenseRecord record = dispenseRepository.findById(dispenseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dispense record not found"));
        logger.debug("Found dispense record - ID: {}, Status: {}", dispenseId, record.getStatus());
        return record;
    }

    private List<DispenseResponseDto> toDtos(List<DispenseRecord> records) {
        List<DispenseResponseDto> response = new ArrayList<>();
        for (DispenseRecord record : records) {
            response.add(toDto(record));
        }
        return response;
    }

    private DispenseResponseDto toDto(DispenseRecord record) {
        InventoryItem item = record.getInventoryItem();

        String patientName = null;
        try {
            if (record.getPatientId() != null) {
                PatientSummaryDto p = patientClient.getPatientById(record.getPatientId());
                if (p != null) {
                    patientName = p.getName();
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to fetch patient name for ID: {}", record.getPatientId(), e);
        }

        String dispensedByName = null;
        try {
            if (record.getDispensedById() != null) {
                UserSummaryDto u = authClient.getUserById(record.getDispensedById());
                if (u != null) {
                    dispensedByName = u.getName();
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to fetch dispensed by user name for ID: {}", record.getDispensedById(), e);
        }

        return new DispenseResponseDto(
                record.getDispenseId(),
                record.getRxId(),
                item.getInventoryId(),
                item.getMedication().getName(),
                item.getBatchNumber(),
                record.getPatientId(),
                patientName,
                record.getDispensedById(),
                dispensedByName,
                record.getQuantity(),
                record.getDispensedAt(),
                record.getStatus().name()
        );
    }
}
