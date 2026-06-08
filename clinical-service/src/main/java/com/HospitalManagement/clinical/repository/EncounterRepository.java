package com.HospitalManagement.clinical.repository;

import com.HospitalManagement.clinical.entity.Encounter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EncounterRepository extends JpaRepository<Encounter, Long> {
}
