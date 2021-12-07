package com.via.ecza.dto;

import com.via.ecza.entity.DrugCard;
import com.via.ecza.entity.PurchaseStatus;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Setter
@Getter
public class DepotPurchaseOrderDrugsDto {
    private Long purchaseOrderDrugsId;
    private Long totalQuantity;
    private Long chargedQuantity;
    private Long incompleteQuantity;
    private Date expirationDate;
    private String purchaseOrderDrugNote;
    private CustomerOrderDto customerOrder;
    private DrugCard drugCard;
    private PurchaseStatus purchaseStatus;

    public DepotPurchaseOrderDrugsDto(){

    }


}
