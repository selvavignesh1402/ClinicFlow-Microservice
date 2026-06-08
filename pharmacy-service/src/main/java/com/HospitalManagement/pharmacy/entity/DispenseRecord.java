package com.HospitalManagement.pharmacy.entity;

import com.HospitalManagement.shared.enums.DispenseStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dispense_record")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispenseRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dispenseId;

    @Column(name = "rx_id", nullable = false)
    private Long rxId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id")
    private InventoryItem inventoryItem;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "dispensed_by_fk")
    private Long dispensedById;

    private Integer quantity;
    private LocalDateTime dispensedAt;

    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DispenseStatus status;
}
