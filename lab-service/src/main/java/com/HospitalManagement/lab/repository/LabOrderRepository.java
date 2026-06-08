package com.HospitalManagement.lab.repository;

import com.HospitalManagement.lab.entity.LabOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LabOrderRepository extends JpaRepository<LabOrder, Long> {
    List<LabOrder> findAllByPatientId(Long patientId);
}
