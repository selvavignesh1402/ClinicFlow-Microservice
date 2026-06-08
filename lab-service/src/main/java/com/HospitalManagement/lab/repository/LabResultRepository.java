package com.HospitalManagement.lab.repository;

import com.HospitalManagement.lab.entity.LabResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LabResultRepository extends JpaRepository<LabResult, Long> {
    List<LabResult> findAllByLabOrderLabOrderId(Long labOrderId);
}
