package com.via.ecza.dto;

import com.sun.istack.NotNull;
import com.via.ecza.entity.CurrencyType;
import com.via.ecza.entity.DrugCard;
import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.validation.constraints.NotEmpty;
import java.util.Date;

@Data
public class CustomerOrderDrugsListDto {

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
    private Long purchaseOrderDrugsId;
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
    private int profit;
    private String purchaseOrderDrugAdminNote;
    private Integer depotCount;

}
