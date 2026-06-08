package com.HospitalManagement.patient.controller;

import com.HospitalManagement.patient.entity.Patient;
import com.HospitalManagement.patient.repository.PatientRepository;
import com.HospitalManagement.shared.dto.PatientSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/internal/patients")
@RequiredArgsConstructor
public class InternalPatientController {

    private final PatientRepository patientRepository;

    @GetMapping("/{id}")
    public PatientSummaryDto getPatientById(@PathVariable Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found with id: " + id));
        return PatientSummaryDto.builder()
                .patientId(patient.getPatientId())
                .mrn(patient.getMrn())
                .name(patient.getName())
                .gender(patient.getGender())
                .status(patient.getStatus())
                .build();
    }
}
