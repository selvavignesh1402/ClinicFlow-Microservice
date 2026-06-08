package com.HospitalManagement.clinical.repository;

import com.HospitalManagement.clinical.entity.Appointment;
import com.HospitalManagement.shared.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.clinicianId = :clinicianId AND a.startAt < :endAt AND a.endAt > :startAt AND a.status NOT IN :excludedStatuses")
    boolean existsByClinicianId(
            @Param("clinicianId") Long clinicianId,
            @Param("endAt") LocalDateTime endAt,
            @Param("startAt") LocalDateTime startAt,
            @Param("excludedStatuses") List<AppointmentStatus> excludedStatuses
    );

    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.clinicianId = :clinicianId AND a.startAt < :endAt AND a.endAt > :startAt AND a.status NOT IN :excludedStatuses AND a.apptId <> :apptId")
    boolean existsByClinicianIdAndApptIdNot(
            @Param("clinicianId") Long clinicianId,
            @Param("endAt") LocalDateTime endAt,
            @Param("startAt") LocalDateTime startAt,
            @Param("excludedStatuses") List<AppointmentStatus> excludedStatuses,
            @Param("apptId") Long apptId
    );

    List<Appointment> findByPatientIdOrderByStartAtDesc(Long patientId);

    List<Appointment> findByClinicianIdAndStartAtBetweenOrderByStartAtAsc(
            Long clinicianId,
            LocalDateTime from,
            LocalDateTime to
    );
}
