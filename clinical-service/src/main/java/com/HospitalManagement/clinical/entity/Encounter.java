package com.HospitalManagement.clinical.entity;

import com.HospitalManagement.shared.enums.EncounterStatus;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "encounters")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Encounter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long encounterId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "clinician_id", nullable = false)
    private Long clinicianId;

    private String visitType;
    private String chiefComplaint;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private String vitalsJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private String notesJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private String diagnosesJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private String ordersJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private String prescriptionsJson;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EncounterStatus status;

    @Column(name = "signed_by")
    private Long signedById;

    private LocalDateTime signedAt;

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = EncounterStatus.IN_PROGRESS;
        }
    }
}
