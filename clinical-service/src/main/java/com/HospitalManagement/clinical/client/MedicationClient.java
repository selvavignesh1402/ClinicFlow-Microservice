package com.HospitalManagement.clinical.client;

import com.HospitalManagement.shared.dto.MedicationSummaryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "pharmacy-service")
public interface MedicationClient {

    @GetMapping("/internal/medications/{id}")
    MedicationSummaryDto getMedicationById(@PathVariable("id") Long id);
}