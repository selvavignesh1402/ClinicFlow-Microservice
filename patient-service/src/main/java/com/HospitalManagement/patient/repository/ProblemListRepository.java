package com.HospitalManagement.patient.repository;

import com.HospitalManagement.patient.entity.ProblemList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemListRepository extends JpaRepository<ProblemList, Long> {
}
