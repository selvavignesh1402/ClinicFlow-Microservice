package com.HospitalManagement.lab.service;

import com.HospitalManagement.lab.client.AuthClient;
import com.HospitalManagement.lab.entity.LabOrder;
import com.HospitalManagement.lab.entity.LabResult;
import com.HospitalManagement.shared.dto.UserSummaryDto;
import com.HospitalManagement.shared.security.AuthenticatedUser;
import com.HospitalManagement.shared.enums.LabOrderStatus;
import com.HospitalManagement.shared.enums.LabResultFlag;
import com.HospitalManagement.lab.repository.LabOrderRepository;
import com.HospitalManagement.lab.repository.LabResultRepository;
import com.HospitalManagement.lab.dto.LabResultRequestDto;
import com.HospitalManagement.lab.dto.LabResultResponseDto;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class LabResultService {

    private static final Logger logger = LoggerFactory.getLogger(LabResultService.class);

    private final LabResultRepository labResultRepository;
    private final LabOrderRepository labOrderRepository;
    private final AuthClient authClient;

    public LabResultService(LabResultRepository labResultRepository,
                            LabOrderRepository labOrderRepository,
                            AuthClient authClient) {
        this.labResultRepository = labResultRepository;
        this.labOrderRepository = labOrderRepository;
        this.authClient = authClient;
    }

    @Transactional(readOnly = true)
    public List<LabResultResponseDto> getAllResults() {
        logger.debug("Fetching all lab results");
        List<LabResult> labResults = labResultRepository.findAll();
        List<LabResultResponseDto> results = toResultResponseList(labResults);
        logger.info("Retrieved {} lab results", results.size());
        return results;
    }

    @Transactional(readOnly = true)
    public LabResultResponseDto getResultById(Long resultId) {
        logger.debug("Fetching lab result by ID: {}", resultId);
        LabResultResponseDto result = toResultResponseDto(findLabResult(resultId));
        logger.info("Retrieved lab result - ID: {}, Test: {}", resultId, result.testCode());
        return result;
    }

    @Transactional(readOnly = true)
    public List<LabResultResponseDto> getResultsByOrderId(Long labOrderId) {
        logger.debug("Fetching lab results for order ID: {}", labOrderId);
        findLabOrder(labOrderId);
        List<LabResult> labResults = labResultRepository.findAllByLabOrderLabOrderId(labOrderId);
        List<LabResultResponseDto> results = toResultResponseList(labResults);
        logger.info("Retrieved {} lab results for order ID: {}", results.size(), labOrderId);
        return results;
    }

    public LabResultResponseDto createResult(LabResultRequestDto requestDto) {
        logger.info("Creating lab result - Order ID: {}, Test: {}", requestDto.labOrderId(), requestDto.testCode());
        LabResult labResult = new LabResult();
        mapRequestToEntity(requestDto, labResult);
        LabResult savedResult = labResultRepository.save(labResult);
        updateLabOrderStatus(savedResult.getLabOrder());
        logger.info("Successfully created lab result - ID: {}", savedResult.getResultId());
        return toResultResponseDto(savedResult);
    }

    public LabResultResponseDto updateResult(Long resultId, LabResultRequestDto requestDto) {
        logger.info("Updating lab result - ID: {}", resultId);
        LabResult labResult = findLabResult(resultId);
        mapRequestToEntity(requestDto, labResult);
        LabResult savedResult = labResultRepository.save(labResult);
        updateLabOrderStatus(savedResult.getLabOrder());
        logger.info("Successfully updated lab result - ID: {}", resultId);
        return toResultResponseDto(savedResult);
    }

    public void deleteResult(Long resultId) {
        logger.info("Deleting lab result - ID: {}", resultId);
        LabResult labResult = findLabResult(resultId);
        LabOrder order = labResult.getLabOrder();
        labResultRepository.delete(labResult);
        updateLabOrderStatus(order);
        logger.info("Successfully deleted lab result - ID: {}", resultId);
    }

    private void mapRequestToEntity(LabResultRequestDto requestDto, LabResult labResult) {
        LabOrder labOrder = findLabOrder(requestDto.labOrderId());

        labResult.setLabOrder(labOrder);
        labResult.setTestCode(requestDto.testCode());
        labResult.setValue(requestDto.value());
        labResult.setUnits(requestDto.units());

        // Store reference range JSON directly (optional field)
        labResult.setReferenceRangeJson(
                requestDto.referenceRangeJson() != null && !requestDto.referenceRangeJson().isBlank()
                        ? requestDto.referenceRangeJson()
                        : null
        );

        // Use provided flag or default to NORMAL
        labResult.setFlag(
                requestDto.flag() != null
                        ? requestDto.flag()
                        : LabResultFlag.NORMAL
        );

        labResult.setReportedAt(
                requestDto.reportedAt() != null
                        ? requestDto.reportedAt()
                        : LocalDateTime.now()
        );

        AuthenticatedUser currentUser = getCurrentUser();
        labResult.setReportedBy(currentUser.getUserId());

        // Ensure resultUri exists on the order
        if (labOrder.getResultUri() == null || labOrder.getResultUri().isBlank()) {
            labOrder.setResultUri(buildResultUri(labOrder.getLabOrderId()));
        }
    }

    private void updateLabOrderStatus(LabOrder labOrder) {
        logger.debug("Updating lab order status - Order ID: {}", labOrder.getLabOrderId());

        List<LabResult> results =
                labResultRepository.findAllByLabOrderLabOrderId(labOrder.getLabOrderId());

        // No results yet
        if (results.isEmpty()) {
            if (labOrder.getCollectedAt() != null) {
                labOrder.setStatus(LabOrderStatus.COLLECTED);
            } else {
                labOrder.setStatus(LabOrderStatus.ORDERED);
            }
            labOrderRepository.save(labOrder);
            logger.debug("Set lab order status to {} (no results)", labOrder.getStatus());
            return;
        }
        // Check if any result is CRITICAL
        boolean hasCritical = false;

        for (LabResult result : results) {
            if (result.getFlag() == LabResultFlag.CRITICAL){
                hasCritical = true;
                break;
            }
        }
        // Update order workflow state
        labOrder.setStatus(
                hasCritical
                        ? LabOrderStatus.CRITICAL_REPORTED
                        : LabOrderStatus.RESULTS_REPORTED
        );
        // Ensure result URI exists
        if (labOrder.getResultUri() == null || labOrder.getResultUri().isBlank()) {
            labOrder.setResultUri(buildResultUri(labOrder.getLabOrderId()));
        }

        labOrderRepository.save(labOrder);
        logger.debug("Updated lab order status to {} - Critical: {}", labOrder.getStatus(), hasCritical);
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

    private LabResult findLabResult(Long resultId) {
        logger.debug("Looking up lab result by ID: {}", resultId);
        return labResultRepository.findById(resultId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Lab result not found with id: " + resultId));
    }

    private AuthenticatedUser getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser) {
            return (AuthenticatedUser) authentication.getPrincipal();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
    }

    private List<LabResultResponseDto> toResultResponseList(List<LabResult> labResults) {
        List<LabResultResponseDto> responseDtos = new ArrayList<>();
        for (LabResult labResult : labResults) {
            responseDtos.add(toResultResponseDto(labResult));
        }
        return responseDtos;
    }

    private LabResultResponseDto toResultResponseDto(LabResult labResult) {
        String reportedByName = null;
        try {
            if (labResult.getReportedBy() != null) {
                UserSummaryDto u = authClient.getUserById(labResult.getReportedBy());
                if (u != null) {
                    reportedByName = u.getName();
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to fetch user name for ID: {}", labResult.getReportedBy(), e);
        }

        return new LabResultResponseDto(
                labResult.getResultId(),
                labResult.getLabOrder().getLabOrderId(),
                labResult.getTestCode(),
                labResult.getValue(),
                labResult.getUnits(),
                labResult.getReferenceRangeJson(),
                labResult.getFlag(),
                labResult.getReportedAt(),
                labResult.getReportedBy(),
                reportedByName
        );
    }
}
