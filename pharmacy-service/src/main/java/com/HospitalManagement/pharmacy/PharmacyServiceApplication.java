package com.HospitalManagement.pharmacy;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients(basePackages = "com.HospitalManagement.pharmacy.client")
@ComponentScan(basePackages = {
        "com.HospitalManagement.pharmacy",
        "com.HospitalManagement.shared"
})
public class PharmacyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PharmacyServiceApplication.class, args);
    }
}