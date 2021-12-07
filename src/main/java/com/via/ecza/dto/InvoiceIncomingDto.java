package com.via.ecza.dto;

import com.via.ecza.entity.*;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
public class InvoiceIncomingDto {

    private Long invoiceId;

    private InvoiceCheckingCardDto checkingCard;
    private InvoiceCheckingCardDto otherCheckingCard;
    private String invoiceNo;

    private User user;

    private FinalReceiptAccountingDto finalReceipt;

    private Date invoiceDate;

    private String taxNo;

    private String taxOffice;

    private String crsNo;

    private int status;

    private Date dueDate;

    private InvoiceStatus invoiceStatus;

    private InvoiceType invoiceType;

    private InvoicePurpose invoicePurpose;

}
