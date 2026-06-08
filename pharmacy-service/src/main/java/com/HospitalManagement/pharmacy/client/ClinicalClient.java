package com.HospitalManagement.pharmacy.client;

import com.HospitalManagement.shared.dto.PrescriptionSummaryDto;
import com.HospitalManagement.shared.enums.PrescriptionStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "clinical-service")
public interface ClinicalClient {

    @GetMapping("/internal/prescriptions/{rxId}")
    PrescriptionSummaryDto getPrescriptionById(@PathVariable("rxId") Long rxId);

    @PatchMapping("/internal/prescriptions/{rxId}/status")
    PrescriptionSummaryDto updatePrescriptionStatus(
            @PathVariable("rxId") Long rxId,
            @RequestParam("status") PrescriptionStatus status
    );
}