package com.via.ecza.dto;

import com.sun.istack.NotNull;
import com.via.ecza.entity.DrugCard;
import com.via.ecza.entity.Receipt;
import com.via.ecza.entity.RefundStatus;
import com.via.ecza.entity.Supplier;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.Date;

@Data
@Getter
@Setter
public class ReceiptRefundDto {

    private Long refundId;
    private Date createdAt;
    private Date expirationDate;
    private Double totalPrice;
    private Float unitPrice;
    private Long totality;
    private String refundOrderNo;
    private DrugCardDto drugCard;
    private SingleSupplierDto supplier;
    private RefundStatus refundStatus;
    //private Receip receipt;
    private Date acceptanceDate;
}
