package com.via.ecza.dto;

import com.via.ecza.entity.CustomerOrderStatus;
import com.via.ecza.entity.DrugCard;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class PurchaseOrderDrugsDto {

    private Long expirationDate;
    private Long totalQuantity;
    private Long chargedQuantity;
    private Long incompleteQuantity;
    private Long unitPrice;
    private CustomerOrderDto customerOrder;
    private CustomerOrderStatus orderStatus;
    private DrugCard drugCard;

}
