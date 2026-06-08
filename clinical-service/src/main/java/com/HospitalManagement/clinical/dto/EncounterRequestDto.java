package com.HospitalManagement.clinical.dto;

import com.HospitalManagement.shared.enums.EncounterStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EncounterRequestDto(
        @NotNull
        Long patientId,

        @NotBlank
        String visitType,

        @NotBlank
        String chiefComplaint,

        @NotBlank
        String vitalsJson,

        @NotBlank
        String notesJson,

        @NotBlank
        String diagnosesJson,

        @NotBlank
        String ordersJson,

        String prescriptionsJson,
        EncounterStatus status
) {
}
