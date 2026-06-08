package com.HospitalManagement.clinical.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record ReportRequestDto(
        @NotBlank String scope,
        String parametersJson,
        String metricsJson,
        LocalDateTime generatedAt,
        @NotBlank String reportUri
) {
}
