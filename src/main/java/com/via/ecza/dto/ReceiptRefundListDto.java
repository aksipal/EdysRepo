package com.via.ecza.dto;

import com.via.ecza.entity.DrugCard;
import com.via.ecza.entity.Receipt;
import com.via.ecza.entity.RefundStatus;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Getter
@Setter
public class ReceiptRefundListDto {

    private Long refundId;
    private Date createdAt;
    private Date expirationDate;
    private Double totalPrice;
    private Float unitPrice;
    private Long totality;
    private DrugCardDto drugCard;
    private Long supplierId;
    private ReceiptSupplierDto supplier;
    private String refundOrderNo;
    private RefundStatus refundStatus;
    private Date acceptanceDate;
    private ReceiptListDto receipt;

}
