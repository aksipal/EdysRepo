package com.via.ecza.dto;


import com.via.ecza.entity.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Data
public class CustomerSuppliersDto {

    private Long customerSupplyOrderId;
    private Date createdAt;
    private Long quantity;
    private Long totality;
    private Long stocks;
    private Float averageUnitPrice;
    private Float unitPrice;
    private Double totalPrice;
    private Long generalPrice;
    private String surplus;
    private Long surplusQuantity;
    private Float supplierProfit;
    private String supplyOrderNo;
    private Float distributorDiscount;
    private Float institutionDiscount;
    private Float producerDiscount;

    private Long depotTotalQuantity;
    private Long depotStockQuantity;

    private SupplyDrugCardDto drugCard;
    private SingleSupplierDto supplier;
    private CustomerSupplyStatus customerSupplyStatus;
    private  PurchaseOrderDrugDto purchaseOrderDrugs;
    public  CustomerSuppliersDto() {


    }



}
