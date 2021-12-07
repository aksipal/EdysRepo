package com.via.ecza.dto;


import com.via.ecza.entity.RefundStatus;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Getter
@Setter
public class PharmacyRefundDto {
    private Long refundId;
    private Date createdAt;
    private Date expirationDate;
    private String refundOrderNo;
    private Double totalPrice;
    private Long totality;
    private Float unitPrice;
    private SupplyDrugCardDto drugCard;
    private SingleSupplierDto supplier;
    private RefundStatus refundStatus;
    private Long otherCompanyId;
    private String otherCompanyName;

    public PharmacyRefundDto() {
    }
}


