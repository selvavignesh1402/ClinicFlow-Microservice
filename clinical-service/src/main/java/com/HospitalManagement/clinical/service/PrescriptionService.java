package com.HospitalManagement.clinical.service;

import com.HospitalManagement.clinical.client.AuthClient;
import com.HospitalManagement.clinical.client.PatientClient;
import com.HospitalManagement.clinical.client.MedicationClient;
import com.HospitalManagement.clinical.entity.Encounter;
import com.HospitalManagement.clinical.entity.Prescription;
import com.HospitalManagement.shared.enums.PrescriptionStatus;
import com.HospitalManagement.clinical.repository.EncounterRepository;
import com.HospitalManagement.clinical.repository.PrescriptionRepository;
import com.HospitalManagement.clinical.dto.PrescriptionRequestDto;
import com.HospitalManagement.clinical.dto.PrescriptionResponseDto;
import com.HospitalManagement.shared.dto.PatientSummaryDto;
import com.HospitalManagement.shared.dto.UserSummaryDto;
import com.HospitalManagement.shared.dto.MedicationSummaryDto;
import com.HospitalManagement.shared.security.AuthenticatedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PrescriptionService {

    private static final Logger logger = LoggerFactory.getLogger(PrescriptionService.class);

    private final PrescriptionRepository prescriptionRepository;
    private final EncounterRepository encounterRepository;
    private final PatientClient patientClient;
    private final AuthClient authClient;
    private final MedicationClient medicationClient;

    public PrescriptionService(
            PrescriptionRepository prescriptionRepository,
            EncounterRepository encounterRepository,
            PatientClient patientClient,
            AuthClient authClient,
            MedicationClient medicationClient
    ) {
        this.prescriptionRepository = prescriptionRepository;
        this.encounterRepository = encounterRepository;
        this.patientClient = patientClient;
        this.authClient = authClient;
        this.medicationClient = medicationClient;
    }

    /* ================= READ ================= */

    @Transactional(readOnly = true)
    public List<PrescriptionResponseDto> getAllPrescriptions() {
        logger.debug("Fetching all prescriptions");
        return prescriptionRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public PrescriptionResponseDto getPrescriptionById(Long rxId) {
        logger.debug("Fetching prescription with ID: {}", rxId);
        return toResponseDto(findPrescription(rxId));
    }

    /* ================= CREATE ================= */

    public PrescriptionResponseDto createPrescription(PrescriptionRequestDto requestDto) {
        logger.info("Creating prescription for PatientID={}, EncounterID={}",
                requestDto.patientId(), requestDto.encounterId());

        Prescription prescription = new Prescription();
        mapRequestToEntity(requestDto, prescription, true);

        return toResponseDto(prescriptionRepository.save(prescription));
    }

    /* ================= UPDATE ================= */

    public PrescriptionResponseDto updatePrescription(Long rxId, PrescriptionRequestDto requestDto) {
        logger.info("Updating prescription RxID={}, NewStatus={}", rxId, requestDto.status());

        Prescription prescription = findPrescription(rxId);

        if (prescription.getStatus() == PrescriptionStatus.ISSUED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Issued prescriptions cannot be modified"
            );
        }

        if (requestDto.status() != null) {
            prescription.setStatus(requestDto.status());
            prescription.setIssuedAt(LocalDateTime.now());
            prescription.setClinicianId(getAuthenticatedClinician().getUserId());
        }

        return toResponseDto(prescriptionRepository.save(prescription));
    }

    /* ================= DELETE ================= */

    public void deletePrescription(Long rxId) {
        logger.info("Deleting prescription RxID={}", rxId);
        prescriptionRepository.delete(findPrescription(rxId));
    }

    /* ================= MAPPING ================= */

    private void mapRequestToEntity(
            PrescriptionRequestDto requestDto,
            Prescription prescription,
            boolean isCreate
    ) {
        prescription.setEncounter(findEncounter(requestDto.encounterId()));
        findPatient(requestDto.patientId());
        findMedication(requestDto.medicationId());

        prescription.setPatientId(requestDto.patientId());
        prescription.setMedId(requestDto.medicationId());
        prescription.setDosage(requestDto.dosage());
        prescription.setFrequency(requestDto.frequency());
        prescription.setDurationDays(requestDto.durationDays());
        prescription.setQuantity(requestDto.quantity());
        prescription.setRepeats(requestDto.repeats());
        prescription.setRoute(requestDto.route());
        prescription.setNotes(requestDto.notes());

        if (requestDto.status() != null) {
            prescription.setStatus(requestDto.status());
        }

        // ✅ Clinician identity from JWT
        prescription.setClinicianId(getAuthenticatedClinician().getUserId());

        // ✅ Server‑controlled issuedAt
        if (isCreate || requestDto.status() == PrescriptionStatus.ISSUED) {
            prescription.setIssuedAt(LocalDateTime.now());
        }
    }

    /* ================= HELPERS ================= */

    private Prescription findPrescription(Long rxId) {
        return prescriptionRepository.findById(rxId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Prescription not found with id: " + rxId));
    }

    private Encounter findEncounter(Long encounterId) {
        return encounterRepository.findById(encounterId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Encounter not found with id: " + encounterId));
    }

    private PatientSummaryDto findPatient(Long patientId) {
        try {
            PatientSummaryDto patient = patientClient.getPatientById(patientId);
            if (patient == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found with id: " + patientId);
            }
            return patient;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found with id: " + patientId);
        }
    }

    private MedicationSummaryDto findMedication(Long medicationId) {
        try {
            MedicationSummaryDto medication = medicationClient.getMedicationById(medicationId);
            if (medication == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Medication not found with id: " + medicationId);
            }
            return medication;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Medication not found with id: " + medicationId);
        }
    }

    private AuthenticatedUser getAuthenticatedClinician() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser authUser) {
            return authUser;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated clinician not found");
    }

    private UserSummaryDto findUser(Long userId) {
        try {
            UserSummaryDto user = authClient.getUserById(userId);
            if (user == null) {
                return null;
            }
            return user;
        } catch (Exception e) {
            return null;
        }
    }

    /* ================= RESPONSE ================= */

    private PrescriptionResponseDto toResponseDto(Prescription prescription) {
        PatientSummaryDto patient = findPatient(prescription.getPatientId());
        UserSummaryDto clinician = findUser(prescription.getClinicianId());
        MedicationSummaryDto medication = findMedication(prescription.getMedId());

        return new PrescriptionResponseDto(
                prescription.getRxId(),
                prescription.getEncounter().getEncounterId(),
                prescription.getPatientId(),
                patient != null ? patient.getName() : null,
                prescription.getClinicianId(),
                clinician != null ? clinician.getName() : null,
                prescription.getMedId(),
                medication != null ? medication.getName() : null,
                prescription.getDosage(),
                prescription.getFrequency(),
                prescription.getDurationDays(),
                prescription.getQuantity(),
                prescription.getRepeats(),
                prescription.getRoute(),
                prescription.getNotes(),
                prescription.getStatus(),
                prescription.getIssuedAt()
        );
    }
}
