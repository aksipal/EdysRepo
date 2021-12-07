package com.via.ecza.dto;


import com.via.ecza.entity.SupplierOfferStatus;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Getter
@Setter
public class SupplyOffersDto {

    private Long supplierOfferId;
    private Long quantity;
    private Long offeredQuantity;
    private Float supplierProfit;
    private Float offeredSupplierProfit;
    private Float offeredAveragePrice;
    private Double offeredTotalPrice;
    private Float averageUnitPrice;
    private Long surplusQuantity;
    private Long offeredSurplusQuantity;
    private Long offeredTotality;
    private Long totalQuantity;
    private Date createdAt;
    private Double totalPrice;
    private String surplus;
    private String offeredSurplus;
    private Double totality;
    private SupplierSearchDto supplier;
    private Float producerDiscount;
    private SupplierOfferStatus supplierOfferStatus;

    public SupplyOffersDto() {
    }

}
