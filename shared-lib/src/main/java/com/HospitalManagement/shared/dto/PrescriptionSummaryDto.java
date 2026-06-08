package com.HospitalManagement.shared.dto;

import com.HospitalManagement.shared.enums.PrescriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionSummaryDto {
    private Long rxId;
    private Long encounterId;
    private Long patientId;
    private Long clinicianId;
    private Long medicationId;
    private String dosage;
    private String frequency;
    private Integer durationDays;
    private Integer quantity;
    private Integer repeats;
    private String route;
    private String notes;
    private PrescriptionStatus status;
    private LocalDateTime issuedAt;
}
