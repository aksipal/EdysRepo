package com.via.ecza.dto;

import com.via.ecza.entity.*;
import lombok.Data;

import java.util.Date;

@Data
public class SingleInvoiceDto {

    private Long invoiceId;
    private String invoiceNo;
    private User user;
    private SingleFinalReceiptDto finalReceipt;
    private Date createdAt;
    private Date invoiceDate;
    private String taxNo;
    private String taxOffice;
    private String crsNo;
    private InvoiceStatus invoiceStatus;
    private InvoiceType invoiceType;
    private InvoicePurpose invoicePurpose;
    private CheckingCardDto checkingCard;
    private CheckingCardDto otherCheckingCard;
}
