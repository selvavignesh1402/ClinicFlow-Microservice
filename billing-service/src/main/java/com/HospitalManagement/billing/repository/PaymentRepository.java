package com.HospitalManagement.billing.repository;

import com.HospitalManagement.billing.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByInvoiceInvoiceId(Long invoiceId);
    Payment findTopByInvoiceInvoiceIdOrderByPaidAtDescPaymentIdDesc(Long invoiceId);
}
