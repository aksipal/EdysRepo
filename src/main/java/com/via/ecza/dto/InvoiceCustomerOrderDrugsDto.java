package com.via.ecza.dto;

import com.via.ecza.entity.CurrencyType;
import com.via.ecza.entity.DrugCard;
import lombok.Data;

@Data
public class InvoiceCustomerOrderDrugsDto {
    private Long drugCardId;
    private Long customerOrderId;
    private Long expirationDate;
    private Long totalQuantity;
    private Long unitPrice;
    private Double currencyFee;
    private InvoiceCustomerOrderDto customerOrder;
    private CurrencyType currencyType;
    private DrugCard drugCard;
    private Double unitCost;
    private Integer surplusOfGoods1;
    private Integer surplusOfGoods2;
    private int isCampaignedDrug;
}
