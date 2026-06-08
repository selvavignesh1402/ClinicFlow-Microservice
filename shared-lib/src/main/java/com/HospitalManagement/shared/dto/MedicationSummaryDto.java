package com.HospitalManagement.shared.dto;

import com.HospitalManagement.shared.enums.MedicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationSummaryDto {
    private Long medId;
    private String code;
    private String name;
    private String formulation;
    private String strength;
    private MedicationStatus status;
}
