package com.via.ecza.dto;

import lombok.Data;

import javax.persistence.Column;

@Data
public class InvoiceComboboxDto {

    public String invoiceNo;
    public Long invoiceId;
    private Double totalPrice;
    private Double totalChargePrice;
    private Double totalPriceCurrency;
    private Double freightCostTl;
    private Double freightCostCurrency;
}
