package com.HospitalManagement.clinical.repository;

import com.HospitalManagement.clinical.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
}
