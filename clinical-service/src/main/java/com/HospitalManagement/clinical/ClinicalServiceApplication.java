package com.HospitalManagement.clinical;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients(basePackages = "com.HospitalManagement.clinical.client")
@ComponentScan(basePackages = {
        "com.HospitalManagement.clinical",
        "com.HospitalManagement.shared"
})
public class ClinicalServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClinicalServiceApplication.class, args);
    }
}