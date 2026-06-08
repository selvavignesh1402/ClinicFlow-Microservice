package com.HospitalManagement.clinical.entity;

import com.HospitalManagement.shared.enums.PrescriptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "prescriptions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rxId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encounter_id", nullable = false)
    private Encounter encounter;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "clinician_id", nullable = false)
    private Long clinicianId;

    @Column(name = "med_id", nullable = false)
    private Long medId;

    private String dosage;
    private String frequency;
    private Integer durationDays;
    private Integer quantity;
    private Integer repeats;
    private String route;
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrescriptionStatus status;

    private LocalDateTime issuedAt;
}
