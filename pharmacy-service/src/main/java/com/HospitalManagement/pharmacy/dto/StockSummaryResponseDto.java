package com.HospitalManagement.pharmacy.dto;

public record StockSummaryResponseDto(
        Long medicationId,
        String medicationName,
        Integer totalQuantity,
        String unit,
        Integer batchCount
) {
}
