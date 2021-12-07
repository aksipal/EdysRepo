package com.via.ecza.dto;

import com.via.ecza.entity.FinalReceiptStatus;
import com.via.ecza.entity.ReceiptType;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class FinalReceiptSingleDto {
    private Long finalReceiptId;
    private String finalReceiptNo;
    private int status;
    private Date createdAt;
    private FinalReceiptInvoiceDto invoice;
    private FinalReceiptSupplierDto supplier;
    private int receiptSize;
    private ReceiptType finalReceiptType;
    private FinalReceiptStatus finalReceiptStatus;
}
