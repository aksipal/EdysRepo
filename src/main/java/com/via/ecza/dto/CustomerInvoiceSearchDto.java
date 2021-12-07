package com.via.ecza.dto;

import lombok.Data;

import java.util.Date;

@Data
public class CustomerInvoiceSearchDto {

    private String invoiceNo;
    private Date createdAt;
    private Long checkingCardId;
    private Long otherCheckingCardId;
}
