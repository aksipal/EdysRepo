package com.via.ecza.dto;

import lombok.Data;

import java.util.Date;

@Data
public class FinalReceiptSearchDto {
    private String finalReceiptNo;
    private Long finalReceiptStatusId;
    private Long supplierId;
    private Date createdAt;
    private String invoiceNo;
    private Long finalReceiptStatus;
}
