package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class CustomerOrderDrugSetPricesDto {
    private String currency;
    private Long customerOrderDrugId;
    private Double unitPrice;
    private Double unitCost;
    private Integer surplusOfGoods1;
    private Integer surplusOfGoods2;
    private Float instutionDiscount;
    private Float generalDiscount;
    private int isCampaignedDrug;
    private int profit;

    public CustomerOrderDrugSetPricesDto(){

    }
}
