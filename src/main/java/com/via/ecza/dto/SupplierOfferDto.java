package com.via.ecza.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Getter
@Setter
public class SupplierOfferDto {

    private Long supplierOfferId;
    private Long quantity;
    private Long offeredQuantity;
    private Float averageUnitPrice;
    private Float unitPrice;
    private Double totalPrice;
    private Float institutionDiscount;
    private Float distributorDiscount;
    private Long generalPrice;
    private String surplus;
    private String offeredSurplus;
    private Double totality;
    private Float supplierProfit;
    private Date createdAt;
    private int status;
    private DrugCardDto drugCard;
    private PurchaseOrderDrugsDto purchaseOrderDrugsId;
    private SupplierSearchDto supplier;
    private SupervisorDto supervisorId;

    public SupplierOfferDto() {
    }

}
