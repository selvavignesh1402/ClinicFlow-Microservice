package com.HospitalManagement.pharmacy.controller;

import com.HospitalManagement.pharmacy.entity.MedicationMaster;
import com.HospitalManagement.pharmacy.repository.MedicationMasterRepository;
import com.HospitalManagement.shared.dto.MedicationSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalPharmacyController {

    private final MedicationMasterRepository medicationRepository;

    @GetMapping("/medications/{id}")
    public MedicationSummaryDto getMedicationById(@PathVariable Long id) {
        MedicationMaster med = medicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medication not found: " + id));
        return MedicationSummaryDto.builder()
                .medId(med.getMedId())
                .code(med.getCode())
                .name(med.getName())
                .formulation(med.getFormulation())
                .strength(med.getStrength())
                .status(med.getStatus())
                .build();
    }
}
