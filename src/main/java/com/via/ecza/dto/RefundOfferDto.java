package com.via.ecza.dto;

import com.via.ecza.entity.RefundOfferStatus;
import com.via.ecza.entity.Supplier;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Getter
@Setter
public class RefundOfferDto {
    private Long refundOfferId;
    private Date createdAt;
    private Date expirationDate;
    private Double totalPrice;
    private Float unitPrice;
    private Long totality;
    private Long offeredTotality;
    private Double offeredTotalPrice;
    private SupplyDrugCardDto drugCard;
    private SingleSupplierDto supplier;
    private RefundOfferStatus refundOfferStatus;
}
