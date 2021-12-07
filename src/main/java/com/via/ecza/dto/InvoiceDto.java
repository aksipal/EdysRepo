package com.via.ecza.dto;

import com.via.ecza.entity.*;
import lombok.Data;

import java.util.Date;

@Data
public class InvoiceDto {

    private Long invoiceId;
    private String invoiceNo;
    private User user;
    private FinalReceiptDto finalReceipt;
    private Date createdAt;
    private Date invoiceDate;
    private String taxNo;
    private String taxOffice;
    private String crsNo;
    private InvoiceStatus invoiceStatus;
    private InvoiceType invoiceType;
    private CheckingCardDto checkingCard;
    private CheckingCardDto otherCheckingCard;
    private InvoicePurpose invoicePurpose;
    private Double chargeLiva;

}
