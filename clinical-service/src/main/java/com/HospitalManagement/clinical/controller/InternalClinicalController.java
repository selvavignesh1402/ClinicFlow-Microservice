package com.HospitalManagement.clinical.controller;

import com.HospitalManagement.clinical.entity.Encounter;
import com.HospitalManagement.clinical.entity.Prescription;
import com.HospitalManagement.clinical.repository.EncounterRepository;
import com.HospitalManagement.clinical.repository.PrescriptionRepository;
import com.HospitalManagement.shared.dto.EncounterSummaryDto;
import com.HospitalManagement.shared.dto.PrescriptionSummaryDto;
import com.HospitalManagement.shared.enums.PrescriptionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalClinicalController {

    private final PrescriptionRepository prescriptionRepository;
    private final EncounterRepository encounterRepository;

    @GetMapping("/prescriptions/{rxId}")
    public PrescriptionSummaryDto getPrescriptionById(@PathVariable Long rxId) {
        Prescription rx = prescriptionRepository.findById(rxId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prescription not found: " + rxId));
        return toPrescriptionSummary(rx);
    }

    @PatchMapping("/prescriptions/{rxId}/status")
    public PrescriptionSummaryDto updatePrescriptionStatus(
            @PathVariable Long rxId,
            @RequestParam PrescriptionStatus status
    ) {
        Prescription rx = prescriptionRepository.findById(rxId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prescription not found: " + rxId));
        rx.setStatus(status);
        Prescription saved = prescriptionRepository.save(rx);
        return toPrescriptionSummary(saved);
    }

    @GetMapping("/encounters/{id}")
    public EncounterSummaryDto getEncounterById(@PathVariable Long id) {
        Encounter enc = encounterRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Encounter not found: " + id));
        return EncounterSummaryDto.builder()
                .encounterId(enc.getEncounterId())
                .patientId(enc.getPatientId())
                .clinicianId(enc.getClinicianId())
                .visitType(enc.getVisitType())
                .chiefComplaint(enc.getChiefComplaint())
                .status(enc.getStatus())
                .startAt(enc.getStartAt())
                .build();
    }

    private PrescriptionSummaryDto toPrescriptionSummary(Prescription rx) {
        return PrescriptionSummaryDto.builder()
                .rxId(rx.getRxId())
                .encounterId(rx.getEncounter().getEncounterId())
                .patientId(rx.getPatientId())
                .clinicianId(rx.getClinicianId())
                .medicationId(rx.getMedId())
                .dosage(rx.getDosage())
                .frequency(rx.getFrequency())
                .durationDays(rx.getDurationDays())
                .quantity(rx.getQuantity())
                .repeats(rx.getRepeats())
                .route(rx.getRoute())
                .notes(rx.getNotes())
                .status(rx.getStatus())
                .issuedAt(rx.getIssuedAt())
                .build();
    }
}
