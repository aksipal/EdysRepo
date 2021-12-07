package com.via.ecza.dto;

import com.via.ecza.entity.CurrencyType;
import com.via.ecza.entity.DrugCard;
import lombok.Data;

import java.util.Date;

@Data
public class CustomerOrderDrugsExcelDto {
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
    private Integer surplusOfGoods1;
    private Integer surplusOfGoods2;
    private Float instutionDiscount;
    private Float generalDiscount;
    private int purchaseOrderStatus;
    private int isDeleted;
    private int isAddedByManager;
    private int isCampaignedDrug;
    private CurrencyType currencyType;
    private Double currencyFee;
    private Double freightCostTl;
    private Double exactFreightCost;
    private String purchaseOrderDrugAdminNote;
    private CustomerReceiptDto customerReceipt;
}
