package com.via.ecza.dto;

import com.via.ecza.entity.DrugCard;
import lombok.Data;

import java.util.Date;

@Data
public class DepotCustomerOrderDrugs {
    private Long customerOrderDrugId;

    private Double unitPrice;

    private Long totalQuantity;

    private Long chargedQuantity;

    private Long incompleteQuantity;

    private Date expirationDate;

    private String currency;

    private String customerOrderDrugNote;

    private CustomerOrderDto customerOrder;

    private DrugCard drugCard;

    private Double unitCost;

//    private Integer surplusOfGoods1;
//
//    private Integer surplusOfGoods2;
}
