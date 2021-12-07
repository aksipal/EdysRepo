package com.via.ecza.dto;


import com.via.ecza.entity.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class SingleInvoiceWithActivityDto {

    private Long invoiceId;
    private String invoiceNo;
    private User user;
    private Date createdAt;
    private Date invoiceDate;
    private String taxNo;
    private String taxOffice;
    private String crsNo;
    private List<AccountActivityDto> accountActivities;
    private InvoiceStatus invoiceStatus;
    private InvoiceType invoiceType;
    private InvoicePurpose invoicePurpose;
    private CheckingCardDto checkingCard;
    private CheckingCardDto otherCheckingCard;
}
