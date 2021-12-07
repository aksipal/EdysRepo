package com.via.ecza.dto;

import lombok.Data;

import java.util.Date;

@Data
public class CustomerSupplyOrderExcelDto {
    private Long customerSupplyOrderId;
    private Date createdAt;
    private Long totalQuantity;
    private Long depotTotalQuantity;
    private Long depotStockQuantity;
    private Long quantity;
    private Float averageUnitPrice;
    private Float unitPrice;
    private Double totalPrice;
    private Float institutionDiscount;
    private Float distributorDiscount;
    private Float vat;
    private Long generalPrice;
    private String dispatchNo;
    private String invoiceNo;
    private String surplus;
    private Long surplusQuantity;
    private String note;
    private Long totality;
    private Float supplierProfit;
    private Long stocks;
    private Float producerDiscount;
    private Long supervisorId;
    private String log_cso;
    private String supplyOrderNo;
    private DrugCardDto drugCard;
    private Date acceptanceDate;
    private ReceiptSupplierDto supplier;
    private FinalReceiptPurchaseOrderDrugsDto purchaseOrderDrugs;
    private SingleReceiptDto receipt;
}
