package com.HospitalManagement.lab.client;

import com.HospitalManagement.shared.dto.EncounterSummaryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "clinical-service")
public interface ClinicalClient {

    @GetMapping("/internal/encounters/{id}")
    EncounterSummaryDto getEncounterById(@PathVariable("id") Long id);
}