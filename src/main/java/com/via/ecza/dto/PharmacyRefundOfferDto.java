package com.via.ecza.dto;

import com.via.ecza.entity.RefundStatus;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Getter
@Setter
public class PharmacyRefundOfferDto {
    private Long refundOfferId;
    private Date createdAt;
    private Date expirationDate;
    private Double totalPrice;
    private Long totality;
    private Float unitPrice;
    private Long offeredTotality;
    private Double offeredTotalPrice;
    private SupplyDrugCardDto drugCard;
    private String refundNote;
    private RefundStatus refundStatus;
    private Long otherCompanyId;
    private String otherCompanyName;
}
