package com.via.ecza.dto;


import com.via.ecza.entity.CustomerSupplyStatus;
import com.via.ecza.entity.DrugCard;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Data
public class OrderAcceptanceCustomerSupplyOrderDto {

    private Long customerSupplyOrderId;
    private Date createdAt;
    private Long quantity;
    private Long stocks;
    private Long surplusQuantity;
    private Long totalQuantity;
    private Long totality;
    private Date expirationDate;
    private CustomerSupplyStatus customerSupplyStatus;
    private CustomerSupplyStatus status;
    private String supplyOrderNo;
    private String supplierName;
    private OrderAcceptanceSupplierDto supplier;
    private DrugCard drugCard;
    private Long drugCount;
    private PurchaseOrderDrugDto purchaseOrderDrugs;
    private Integer quantityOfDepot;

    public OrderAcceptanceCustomerSupplyOrderDto() {
        
    }

}
