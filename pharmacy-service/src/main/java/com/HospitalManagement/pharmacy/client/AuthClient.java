package com.HospitalManagement.pharmacy.client;

import com.HospitalManagement.shared.dto.UserSummaryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service")
public interface AuthClient {

    @GetMapping("/internal/users/{id}")
    UserSummaryDto getUserById(@PathVariable("id") Long id);
}