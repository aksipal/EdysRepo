package com.via.ecza.dto;

import lombok.Data;

import java.util.Date;

@Data
public class SinglePurchaseOrderDrugDto {

    private Long purchaseOrderDrugId;
    private Date expirationDate;
    private Long totalQuantity;
    private Long chargedQuantity;
    private Long incompleteQuantity;
    private Float exporterUnitPrice;
    private SupplyCustomerOrderDto customerOrder;
    private SupplyDrugCardDto drugCard;
    private String purchaseOrderDrugNote;
    private String purchaseOrderDrugExportNote;
    private String purchaseOrderDrugAdminNote;
}
