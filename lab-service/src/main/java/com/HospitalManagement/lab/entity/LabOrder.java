package com.HospitalManagement.lab.entity;

import com.HospitalManagement.shared.enums.LabOrderStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lab_order")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long labOrderId;

    @Column(name = "encounter_id")
    private Long encounterId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "ordered_by_fk")
    private Long orderedBy;

    @Column(columnDefinition = "json")
    private String testsJson;

    private String sampleId;
    private LocalDateTime collectedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LabOrderStatus status;
    private String resultUri;
}
