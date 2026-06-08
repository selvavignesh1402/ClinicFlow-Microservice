package com.HospitalManagement.clinical.entity;

import com.HospitalManagement.shared.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long apptId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "clinician_id", nullable = false)
    private Long clinicianId;

    private String department;
    private String serviceType;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    @Column(name = "created_by", nullable = false)
    private Long createdById;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = AppointmentStatus.SCHEDULED;
        }
    }
}
