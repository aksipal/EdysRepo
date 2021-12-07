package com.via.ecza.dto;

import com.via.ecza.entity.ReceiptContent;
import com.via.ecza.entity.ReceiptStatus;
import com.via.ecza.entity.ReceiptType;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class SingleReceiptDto {
    private Long receiptId;
    private String receiptNo;
    private String pharmacyReceiptNo;
    private Date createdAt;
    private Date dueDate;
    private String dispatchNo;
    private Date dispatchDate;
    private ReceiptType receiptType;
    private ReceiptStatus receiptStatus;
    private String invoiceNo;
    private Date invoiceDate;
    private String receiptNote;
    private int status;
    private Long invoiceId;
    private ReceiptSupplierDto supplier;
    private Long totality;
    private Double totalPrice;
    private FinalReceiptSingleDto finalReceipt;
    private List<ReceiptContent> receiptContents;
}
