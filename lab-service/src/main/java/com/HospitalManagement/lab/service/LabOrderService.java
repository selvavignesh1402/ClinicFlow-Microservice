package com.HospitalManagement.lab.service;

import com.HospitalManagement.lab.client.AuthClient;
import com.HospitalManagement.lab.client.ClinicalClient;
import com.HospitalManagement.lab.client.PatientClient;
import com.HospitalManagement.lab.entity.LabOrder;
import com.HospitalManagement.lab.entity.LabResult;
import com.HospitalManagement.shared.dto.EncounterSummaryDto;
import com.HospitalManagement.shared.dto.PatientSummaryDto;
import com.HospitalManagement.shared.dto.UserSummaryDto;
import com.HospitalManagement.shared.security.AuthenticatedUser;
import com.HospitalManagement.lab.repository.LabOrderRepository;
import com.HospitalManagement.lab.repository.LabResultRepository;
import com.HospitalManagement.lab.dto.LabOrderRequestDto;
import com.HospitalManagement.lab.dto.LabOrderResponseDto;
import com.HospitalManagement.lab.dto.LabResultSummaryDto;
import com.HospitalManagement.shared.enums.LabOrderStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class LabOrderService {

    private static final Logger logger = LoggerFactory.getLogger(LabOrderService.class);

    private final LabOrderRepository labOrderRepository;
    private final LabResultRepository labResultRepository;
    private final PatientClient patientClient;
    private final AuthClient authClient;
    private final ClinicalClient clinicalClient;

    public LabOrderService(LabOrderRepository labOrderRepository,
                           LabResultRepository labResultRepository,
                           PatientClient patientClient,
                           AuthClient authClient,
                           ClinicalClient clinicalClient) {
        this.labOrderRepository = labOrderRepository;
        this.labResultRepository = labResultRepository;
        this.patientClient = patientClient;
        this.authClient = authClient;
        this.clinicalClient = clinicalClient;
    }

    @Transactional(readOnly = true)
    public List<LabOrderResponseDto> getAllOrders() {
        logger.debug("Fetching all lab orders");
        List<LabOrder> labOrders = labOrderRepository.findAll();
        List<LabOrderResponseDto> orders = toOrderResponseList(labOrders);
        logger.info("Retrieved {} lab orders", orders.size());
        return orders;
    }

    @Transactional(readOnly = true)
    public LabOrderResponseDto getOrderById(Long labOrderId) {
        logger.debug("Fetching lab order by ID: {}", labOrderId);
        LabOrderResponseDto order = toOrderResponseDto(findLabOrder(labOrderId));
        logger.info("Retrieved lab order - ID: {}, Status: {}", labOrderId, order.status());
        return order;
    }

    @Transactional(readOnly = true)
    public List<LabOrderResponseDto> getOrdersByPatientId(Long patientId) {
        logger.debug("Fetching lab orders for patient ID: {}", patientId);
        validatePatientExists(patientId);
        List<LabOrder> labOrders = labOrderRepository.findAllByPatientId(patientId);
        List<LabOrderResponseDto> orders = toOrderResponseList(labOrders);
        logger.info("Retrieved {} lab orders for patient ID: {}", orders.size(), patientId);
        return orders;
    }

    public LabOrderResponseDto createOrder(LabOrderRequestDto requestDto) {
        logger.info("Creating lab order - Encounter ID: {}", requestDto.encounterId());

        AuthenticatedUser currentUser = getCurrentUser();
        EncounterSummaryDto encounter = findEncounter(requestDto.encounterId());

        LabOrder labOrder = new LabOrder();
        labOrder.setEncounterId(encounter.getEncounterId());
        labOrder.setPatientId(encounter.getPatientId());   // from encounter
        labOrder.setOrderedBy(currentUser.getUserId());    // logged-in user
        labOrder.setTestsJson(requestDto.testsJson());
        labOrder.setSampleId(resolveSampleId(requestDto.sampleId()));
        labOrder.setCollectedAt(requestDto.collectedAt());
        labOrder.setStatus(LabOrderStatus.ORDERED);

        LabOrder savedOrder = labOrderRepository.save(labOrder);

        if (savedOrder.getResultUri() == null || savedOrder.getResultUri().isBlank()) {
            savedOrder.setResultUri(buildResultUri(savedOrder.getLabOrderId()));
            savedOrder = labOrderRepository.save(savedOrder);
        }

        logger.info("Successfully created lab order - ID: {}", savedOrder.getLabOrderId());
        return toOrderResponseDto(savedOrder);
    }

    public LabOrderResponseDto updateOrder(Long labOrderId, LabOrderRequestDto requestDto) {
        logger.info("Updating lab order - ID: {}", labOrderId);
        LabOrder labOrder = findLabOrder(labOrderId);

        if (labOrder.getStatus() != LabOrderStatus.ORDERED) {
            logger.warn("Attempted to update lab order not in ORDERED status - ID: {}, Status: {}", labOrderId, labOrder.getStatus());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Lab order cannot be modified after sample collection"
            );
        }

        // Only allow updating tests before collection
        labOrder.setTestsJson(requestDto.testsJson());
        LabOrder updatedOrder = labOrderRepository.save(labOrder);
        logger.info("Successfully updated lab order - ID: {}", labOrderId);
        return toOrderResponseDto(updatedOrder);
    }

    public LabOrderResponseDto cancelOrder(Long labOrderId) {
        logger.info("Cancelling lab order - ID: {}", labOrderId);
        LabOrder labOrder = findLabOrder(labOrderId);

        if (labOrder.getStatus() == LabOrderStatus.RESULTS_REPORTED ||
                labOrder.getStatus() == LabOrderStatus.CRITICAL_REPORTED) {

            logger.warn("Attempted to cancel lab order with reported results - ID: {}, Status: {}", labOrderId, labOrder.getStatus());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot cancel a lab order with reported results"
            );
        }

        labOrder.setStatus(LabOrderStatus.CANCELLED);
        LabOrder cancelledOrder = labOrderRepository.save(labOrder);
        logger.info("Successfully cancelled lab order - ID: {}", labOrderId);
        return toOrderResponseDto(cancelledOrder);
    }

    public LabOrderResponseDto collectSample(Long labOrderId) {
        logger.info("Collecting sample for lab order - ID: {}", labOrderId);
        LabOrder labOrder = findLabOrder(labOrderId);

        if (labOrder.getStatus() != LabOrderStatus.ORDERED) {
            logger.warn("Attempted to collect sample for non-ordered lab order - ID: {}, Status: {}", labOrderId, labOrder.getStatus());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sample can only be collected once"
            );
        }

        labOrder.setCollectedAt(LocalDateTime.now());
        labOrder.setStatus(LabOrderStatus.COLLECTED);
        LabOrder collectedOrder = labOrderRepository.save(labOrder);
        logger.info("Successfully collected sample for lab order - ID: {}", labOrderId);
        return toOrderResponseDto(collectedOrder);
    }

    private String resolveSampleId(String requestedSampleId) {
        if (requestedSampleId == null || requestedSampleId.isBlank()) {
            return generateSampleId();
        }
        return requestedSampleId;
    }

    private String generateSampleId() {
        return "SMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String buildResultUri(Long labOrderId) {
        if (labOrderId == null) {
            return null;
        }
        return "/api/v1/lab/orders/" + labOrderId + "/results";
    }

    private LabOrder findLabOrder(Long labOrderId) {
        logger.debug("Looking up lab order by ID: {}", labOrderId);
        return labOrderRepository.findById(labOrderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Lab order not found with id: " + labOrderId));
    }

    private EncounterSummaryDto findEncounter(Long encounterId) {
        logger.debug("Looking up encounter by ID: {}", encounterId);
        try {
            return clinicalClient.getEncounterById(encounterId);
        } catch (Exception e) {
            logger.error("Encounter not found with id: {}", encounterId, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Encounter not found with id: " + encounterId);
        }
    }

    private void validatePatientExists(Long patientId) {
        logger.debug("Validating patient by ID: {}", patientId);
        try {
            patientClient.getPatientById(patientId);
        } catch (Exception e) {
            logger.error("Patient not found with id: {}", patientId, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found with id: " + patientId);
        }
    }

    private AuthenticatedUser getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser) {
            return (AuthenticatedUser) authentication.getPrincipal();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
    }

    private List<LabOrderResponseDto> toOrderResponseList(List<LabOrder> labOrders) {
        List<LabOrderResponseDto> responseDtos = new ArrayList<>();
        for (LabOrder labOrder : labOrders) {
            responseDtos.add(toOrderResponseDto(labOrder));
        }
        return responseDtos;
    }

    private LabOrderResponseDto toOrderResponseDto(LabOrder labOrder) {
        List<LabResult> labResults = labResultRepository.findAllByLabOrderLabOrderId(labOrder.getLabOrderId());
        List<LabResultSummaryDto> results = new ArrayList<>();

        for (LabResult result : labResults) {
            results.add(new LabResultSummaryDto(
                    result.getResultId(),
                    result.getTestCode(),
                    result.getValue(),
                    result.getUnits(),
                    result.getFlag(),
                    result.getReportedAt()
            ));
        }

        String patientName = null;
        try {
            PatientSummaryDto p = patientClient.getPatientById(labOrder.getPatientId());
            if (p != null) {
                patientName = p.getName();
            }
        } catch (Exception e) {
            logger.warn("Failed to fetch patient name for ID: {}", labOrder.getPatientId(), e);
        }

        String orderedByName = null;
        try {
            if (labOrder.getOrderedBy() != null) {
                UserSummaryDto u = authClient.getUserById(labOrder.getOrderedBy());
                if (u != null) {
                    orderedByName = u.getName();
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to fetch user name for ID: {}", labOrder.getOrderedBy(), e);
        }

        return new LabOrderResponseDto(
                labOrder.getLabOrderId(),
                labOrder.getEncounterId(),
                labOrder.getPatientId(),
                patientName,
                labOrder.getOrderedBy(),
                orderedByName,
                labOrder.getTestsJson(),
                labOrder.getSampleId(),
                labOrder.getCollectedAt(),
                labOrder.getStatus(),
                labOrder.getResultUri(),
                results
        );
    }
}
