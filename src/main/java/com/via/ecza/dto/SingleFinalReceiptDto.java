package com.via.ecza.dto;

import com.via.ecza.entity.ReceiptType;
import lombok.Data;

import java.util.Date;

@Data
public class SingleFinalReceiptDto {

    private Long finalReceiptId;
    private String finalReceiptNo;
    private int status;
    private Date createdAt;
    private FinalReceiptSupplierDto supplier;
    private ReceiptType finalReceiptType;
}
