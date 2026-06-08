package com.HospitalManagement.lab.dto;

import com.HospitalManagement.shared.enums.LabResultFlag;
import java.time.LocalDateTime;

public record LabResultResponseDto(
        Long resultId,
        Long labOrderId,
        String testCode,
        String value,
        String units,
        String referenceRangeJson,
        LabResultFlag flag,
        LocalDateTime reportedAt,
        Long reportedById,
        String reportedByName
) {
}
