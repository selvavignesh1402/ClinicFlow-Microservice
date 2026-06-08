package com.HospitalManagement.clinical.repository;

import com.HospitalManagement.clinical.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
