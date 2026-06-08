package com.HospitalManagement.billing.entity;

import com.HospitalManagement.shared.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoice")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "encounter_id")
    private Long encounterId; // Can be null

    @Column(columnDefinition = "json")
    private String lineItemsJson;

    private Double subtotal;
    private Double taxes;
    private Double discounts;
    private Double totalAmount;

    private LocalDateTime issuedAt;
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status;
}
