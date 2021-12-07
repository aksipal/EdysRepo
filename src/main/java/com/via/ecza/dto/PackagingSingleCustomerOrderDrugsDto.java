package com.via.ecza.dto;

import com.via.ecza.entity.DrugCard;
import lombok.Data;

import java.util.Date;
@Data
public class PackagingSingleCustomerOrderDrugsDto {
    private Long customerOrderDrugId;
    private Double unitPrice;
    private Double unitCost;
    private Integer surplusOfGoods1;
    private Integer surplusOfGoods2;
    private String currency;
    private Long totalQuantity;
    private Long chargedQuantity;
    private Long incompleteQuantity;
    private Date expirationDate;
    private String customerOrderDrugNote;
    private DrugCard drugCard;
}
