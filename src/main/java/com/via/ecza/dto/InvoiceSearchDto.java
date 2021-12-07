package com.via.ecza.dto;

import lombok.Data;

import java.util.Date;

@Data
public class InvoiceSearchDto {

    private String invoiceNoInComing;
    private String invoicePurposeInComing;
    private String biller;
    private Date invoiceDateInComing;

    private String invoiceNoOutGoing;
    private String invoicePurposeOutGoing;
    private String billed;
    private Date invoiceDateOutGoing;
    private Long otherCompanyId;

    private Long invoiceStatusId;

}
