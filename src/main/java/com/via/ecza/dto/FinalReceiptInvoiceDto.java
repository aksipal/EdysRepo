package com.via.ecza.dto;

import com.via.ecza.entity.*;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
public class FinalReceiptInvoiceDto {

    private Long invoiceId;
    private String invoiceNo;
    private User user;
    private Date invoiceCreatedDate;
    private Date invoiceDate;
    private String taxNo;
    private String taxOffice;
    private String crsNo;
    private Date dueDate;
    private InvoiceStatus invoiceStatus;
    private InvoiceType invoiceType;
    private InvoicePurpose invoicePurpose;
    private Date createdAt;
}
