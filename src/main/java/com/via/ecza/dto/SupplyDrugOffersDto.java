package com.via.ecza.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Getter
@Setter
public class SupplyDrugOffersDto {

    private Long supplierOfferId;
    private Long quantity;
    private Long offeredQuantity;
    private Long offeredTotality;
    private Double offeredTotalPrice;
    private Float offeredAveragePrice;
    private Float unitPrice;
    private Double totalPrice;
    private Float institutionDiscount;
    private Float distributorDiscount;
    private String surplus;
    private String offeredSurplus;
    private Long offeredSurplusQuantity;
    private String note;
    private Double totality;
    private Float offeredSupplierProfit;
    private Date createdAt;
    private SupplyDrugCardDto drugCard;
    private SupplierSearchDto supplier;
    private PurchaseOrderDrugsDto purchaseOrderDrugs;
    private Float producerDiscount;
    private Long otherCompanyId;
    private String otherCompanyName;
}
