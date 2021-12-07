package com.via.ecza.dto;

import com.via.ecza.entity.DrugCard;
import com.via.ecza.entity.PurchaseStatus;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Getter
@Setter
public class PurchaseOrderDrugDto {

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
    private PurchaseStatus purchaseStatus;
}
