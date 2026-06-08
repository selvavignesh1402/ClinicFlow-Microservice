package com.HospitalManagement.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientSummaryDto {
    private Long patientId;
    private String mrn;
    private String name;
    private String gender;
    private String status;
}
