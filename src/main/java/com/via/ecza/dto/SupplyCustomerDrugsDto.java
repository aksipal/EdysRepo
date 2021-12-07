package com.via.ecza.dto;

import com.via.ecza.entity.DrugCard;
import com.via.ecza.entity.PurchaseStatus;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Data
public class SupplyCustomerDrugsDto {
    private Long purchaseOrderDrugsId;
    private Date expirationDate;
    private Long TotalQuantity;
    private Long ChargedQuantity;
    private Long IncompleteQuantity;
    private DrugCard drugCard;
    private PurchaseStatus purchaseStatus;
    private Integer sumOfOffers;
    private CustomerOrderDto customerOrder;
}
