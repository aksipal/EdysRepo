package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Getter
@Setter
public class ReceiptSearchDto {

    private String receiptNo;
    private Long receiptStatusId;
    private Long supplierId;
    private Date createdAt;
    private String invoiceNo;
    private String supplierOrderNo;

}
