package com.HospitalManagement.billing.service;

import com.HospitalManagement.billing.client.ClinicalClient;
import com.HospitalManagement.billing.client.PatientClient;
import com.HospitalManagement.billing.entity.Invoice;
import com.HospitalManagement.shared.dto.EncounterSummaryDto;
import com.HospitalManagement.shared.dto.PatientSummaryDto;
import com.HospitalManagement.shared.enums.InvoiceStatus;
import com.HospitalManagement.shared.exception.ResourceNotFoundException;
import com.HospitalManagement.billing.repository.InvoiceRepository;
import com.HospitalManagement.billing.dto.InvoiceRequestDto;
import com.HospitalManagement.billing.dto.InvoiceResponseDto;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceService {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceService.class);

    private final InvoiceRepository invoiceRepository;
    private final PatientClient patientClient;
    private final ClinicalClient clinicalClient;

    public InvoiceResponseDto createInvoice(InvoiceRequestDto requestDto) {
        logger.info("Creating invoice - Patient ID: {}, Amount: {}", requestDto.patientId(), requestDto.totalAmount());
        validatePatientExists(requestDto.patientId());
        if (requestDto.encounterId() != null) {
            validateEncounterExists(requestDto.encounterId());
        }

        Invoice invoice = new Invoice();
        fillInvoice(invoice, requestDto);
        invoice.setIssuedAt(LocalDateTime.now());
        invoice.setDueDate(requestDto.dueDate() == null ? LocalDateTime.now().plusDays(1) : requestDto.dueDate());
        invoice.setStatus(InvoiceStatus.UNPAID);
        Invoice savedInvoice = invoiceRepository.save(invoice);
        logger.info("Successfully created invoice - ID: {}, Amount: {}", savedInvoice.getInvoiceId(), requestDto.totalAmount());
        return toResponse(savedInvoice);
    }

    public InvoiceResponseDto updateInvoice(Long invoiceId, InvoiceRequestDto requestDto) {
        logger.info("Updating invoice - ID: {}", invoiceId);
        Invoice invoice = findInvoice(invoiceId);
        validatePatientExists(requestDto.patientId());
        if (requestDto.encounterId() != null) {
            validateEncounterExists(requestDto.encounterId());
        }

        fillInvoice(invoice, requestDto);
        if (requestDto.issuedAt() != null) {
            invoice.setIssuedAt(requestDto.issuedAt());
        }
        if (requestDto.dueDate() != null) {
            invoice.setDueDate(requestDto.dueDate());
        }
        if (requestDto.status() != null && !requestDto.status().isBlank()) {
            invoice.setStatus(InvoiceStatus.valueOf(requestDto.status().toUpperCase()));
        }

        Invoice updatedInvoice = invoiceRepository.save(invoice);
        logger.info("Successfully updated invoice - ID: {}", invoiceId);
        return toResponse(updatedInvoice);
    }

    public void deleteInvoice(Long invoiceId) {
        logger.info("Deleting invoice - ID: {}", invoiceId);
        invoiceRepository.delete(findInvoice(invoiceId));
        logger.info("Successfully deleted invoice - ID: {}", invoiceId);
    }

    @Transactional(readOnly = true)
    public InvoiceResponseDto getInvoice(Long invoiceId) {
        logger.debug("Fetching invoice by ID: {}", invoiceId);
        InvoiceResponseDto invoice = toResponse(findInvoice(invoiceId));
        logger.info("Retrieved invoice - ID: {}, Amount: {}", invoiceId, invoice.totalAmount());
        return invoice;
    }

    @Transactional(readOnly = true)
    public Invoice getInvoiceEntity(Long invoiceId) {
        logger.debug("Fetching invoice entity by ID: {}", invoiceId);
        Invoice invoice = findInvoice(invoiceId);
        logger.debug("Retrieved invoice entity - ID: {}", invoiceId);
        return invoice;
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponseDto> getAllInvoices() {
        logger.debug("Fetching all invoices");
        List<Invoice> invoices = invoiceRepository.findAll();
        List<InvoiceResponseDto> response = new ArrayList<>();
        for (Invoice invoice : invoices) {
            response.add(toResponse(invoice));
        }
        logger.info("Retrieved {} invoices", response.size());
        return response;
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponseDto> getPatientInvoices(Long patientId) {
        logger.debug("Fetching invoices for patient ID: {}", patientId);
        validatePatientExists(patientId);
        List<Invoice> invoices = invoiceRepository.findByPatientId(patientId);
        List<InvoiceResponseDto> response = new ArrayList<>();
        for (Invoice invoice : invoices) {
            response.add(toResponse(invoice));
        }
        logger.info("Retrieved {} invoices for patient ID: {}", response.size(), patientId);
        return response;
    }

    public InvoiceResponseDto updateInvoiceStatus(Long invoiceId, String status) {
        logger.info("Updating invoice status - ID: {}, Status: {}", invoiceId, status);
        Invoice invoice = findInvoice(invoiceId);
        if (status != null) {
            invoice.setStatus(InvoiceStatus.valueOf(status.toUpperCase()));
        }
        Invoice updatedInvoice = invoiceRepository.save(invoice);
        logger.info("Successfully updated invoice status - ID: {}, Status: {}", invoiceId, status);
        return toResponse(updatedInvoice);
    }

    private void fillInvoice(Invoice invoice, InvoiceRequestDto requestDto) {
        invoice.setPatientId(requestDto.patientId());
        invoice.setEncounterId(requestDto.encounterId());
        invoice.setLineItemsJson(requestDto.lineItemsJson());
        invoice.setSubtotal(requestDto.subtotal());
        invoice.setTaxes(requestDto.taxes());
        invoice.setDiscounts(requestDto.discounts());
        if (requestDto.totalAmount() == null) {
            invoice.setTotalAmount(requestDto.subtotal() + requestDto.taxes() - requestDto.discounts());
        } else {
            invoice.setTotalAmount(requestDto.totalAmount());
        }
    }

    private Invoice findInvoice(Long invoiceId) {
        logger.debug("Looking up invoice by ID: {}", invoiceId);
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));
    }

    private void validatePatientExists(Long patientId) {
        logger.debug("Validating patient by ID: {}", patientId);
        try {
            patientClient.getPatientById(patientId);
        } catch (Exception e) {
            logger.error("Patient not found with id: {}", patientId, e);
            throw new ResourceNotFoundException("Patient not found with id: " + patientId);
        }
    }

    private void validateEncounterExists(Long encounterId) {
        logger.debug("Validating encounter by ID: {}", encounterId);
        try {
            clinicalClient.getEncounterById(encounterId);
        } catch (Exception e) {
            logger.error("Encounter not found with id: {}", encounterId, e);
            throw new ResourceNotFoundException("Encounter not found with id: " + encounterId);
        }
    }

    private InvoiceResponseDto toResponse(Invoice invoice) {
        String patientName = null;
        try {
            PatientSummaryDto p = patientClient.getPatientById(invoice.getPatientId());
            if (p != null) {
                patientName = p.getName();
            }
        } catch (Exception e) {
            logger.warn("Failed to fetch patient name for ID: {}", invoice.getPatientId(), e);
        }

        return new InvoiceResponseDto(
                invoice.getInvoiceId(),
                invoice.getPatientId(),
                patientName,
                invoice.getEncounterId(),
                invoice.getLineItemsJson(),
                invoice.getSubtotal(),
                invoice.getTaxes(),
                invoice.getDiscounts(),
                invoice.getTotalAmount(),
                invoice.getIssuedAt(),
                invoice.getDueDate(),
                invoice.getStatus()
        );
    }
}
