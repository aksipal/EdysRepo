package com.via.ecza.dto;

import com.via.ecza.entity.*;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
public class CustomerReceiptDto {

    private Long customerReceiptId;
    private String receiptNo;
    private Date createdAt;
    private FinalReceiptSingleDto finalReceipt;
    private ReceiptType receiptType;
    private ReceiptStatus receiptStatus;
    private int status;
    private Long invoiceId;
}
