package com.HospitalManagement.lab;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients(basePackages = "com.HospitalManagement.lab.client")
@ComponentScan(basePackages = {
        "com.HospitalManagement.lab",
        "com.HospitalManagement.shared"
})
public class LabServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LabServiceApplication.class, args);
    }
}