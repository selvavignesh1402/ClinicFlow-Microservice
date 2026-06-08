package com.HospitalManagement.pharmacy.repository;

import com.HospitalManagement.pharmacy.entity.DispenseRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DispenseRecordRepository extends JpaRepository<DispenseRecord, Long> {
    List<DispenseRecord> findByRxId(Long rxId);
    List<DispenseRecord> findByPatientId(Long patientId);
    List<DispenseRecord> findByDispensedById(Long dispensedById);
}
