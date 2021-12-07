package com.via.ecza.dto;

import com.via.ecza.entity.CurrencyType;
import com.via.ecza.entity.DrugCard;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;

@Data
@Setter
@Getter
public class CustomerOrderDrugsDto {
    private Long customerOrderDrugId;
    private Long drugCardId;
    private Long customerOrderId;
    private Long expirationDate;
    private Long totalQuantity;
    private Double unitPrice;
    private CustomerOrderDto customerOrder;
    private DrugCard drugCard;
    private Double unitCost;
    private CurrencyType currencyType;
    private Double currencyFee;
    private Integer surplusOfGoods1;
    private Integer surplusOfGoods2;
    private int isCampaignedDrug;
    private Integer depotCount;

    public CustomerOrderDrugsDto(){

    }


}
