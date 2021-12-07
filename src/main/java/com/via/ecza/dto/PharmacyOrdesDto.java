package com.via.ecza.dto;


import com.via.ecza.entity.CustomerSupplyStatus;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;


@Data
@Getter
@Setter
public class PharmacyOrdesDto {

    private Long customerSupplyOrderId;
    private Date createdAt;
    private Long totality;
    private Long quantity;
    private Long stocks;
    private Long surplusQuantity;
    private DrugCardDto drugCard;
    private Double totalPrice;
    private String surplus;
    private String supplyOrderNo;
    private CustomerOrderDrugsSingleDto customerOrderDrugs;
    private CustomerSupplyStatus customerSupplyStatus;
    private String statusForPharmacy;
    private SinglePurchaseOrderDrugDto purchaseOrderDrugs;
    private Long otherCompanyId;
    private String otherCompanyName;


    public PharmacyOrdesDto() {
    }

}
