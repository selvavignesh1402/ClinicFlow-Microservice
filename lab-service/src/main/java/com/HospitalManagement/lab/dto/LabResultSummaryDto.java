package com.HospitalManagement.lab.dto;

import com.HospitalManagement.shared.enums.LabResultFlag;
import java.time.LocalDateTime;

public record LabResultSummaryDto(
        Long resultId,
        String testCode,
        String value,
        String units,
        LabResultFlag flag,
        LocalDateTime reportedAt
) {
}
