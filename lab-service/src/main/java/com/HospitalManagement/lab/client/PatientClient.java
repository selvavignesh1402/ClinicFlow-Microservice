package com.HospitalManagement.lab.client;

import com.HospitalManagement.shared.dto.PatientSummaryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "patient-service")
public interface PatientClient {

    @GetMapping("/internal/patients/{id}")
    PatientSummaryDto getPatientById(@PathVariable("id") Long id);
}