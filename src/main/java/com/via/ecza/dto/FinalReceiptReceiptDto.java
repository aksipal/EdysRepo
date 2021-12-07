package com.via.ecza.dto;

import com.via.ecza.entity.*;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
public class FinalReceiptReceiptDto {
    private Long receiptId;
    private String receiptNo;
    private String pharmacyReceiptNo;
    private List<CustomerSupplyOrderDrugListDto> customerSupplyOrders;
    private Date createdAt;
    private Date dueDate;
    private String dispatchNo;
    private Date dispatchDate;
    private List<ReceiptRefundDto> refunds;
    private ReceiptType receiptType;
    private ReceiptStatus receiptStatus;
    private int status;
    private Long invoiceId;
    private ReceiptSupplierDto supplier;
    private Long totality;
    private Double totalPrice;
    private SingleFinalReceiptDDto finalReceipt;
}
