package com.HospitalManagement.lab.dto;

import com.HospitalManagement.shared.enums.LabOrderStatus;
import java.time.LocalDateTime;
import java.util.List;

public record LabOrderResponseDto(
        Long labOrderId,
        Long encounterId,
        Long patientId,
        String patientName,
        Long orderedById,
        String orderedByName,
        String testsJson,
        String sampleId,
        LocalDateTime collectedAt,
        LabOrderStatus status,
        String resultUri,
        List<LabResultSummaryDto> results
) {
}
