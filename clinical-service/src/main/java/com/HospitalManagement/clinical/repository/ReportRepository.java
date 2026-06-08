package com.HospitalManagement.clinical.repository;

import com.HospitalManagement.clinical.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
