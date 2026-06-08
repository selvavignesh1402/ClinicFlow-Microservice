package com.HospitalManagement.shared.dto;

import com.HospitalManagement.shared.enums.EncounterStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EncounterSummaryDto {
    private Long encounterId;
    private Long patientId;
    private Long clinicianId;
    private String visitType;
    private String chiefComplaint;
    private EncounterStatus status;
    private LocalDateTime startAt;
}
