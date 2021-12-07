package com.via.ecza.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Getter
@Setter
public class ReceiptInvoiceDto {

    private Long invoiceId;
    private String invoiceNo;
    private Date invoiceCreatedDate;
    private Date invoiceDate;
}
