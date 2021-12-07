package com.via.ecza.dto;


import com.via.ecza.entity.SupplyOrderPrice;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Data
public class CustomerSupplyOrderDto {

    private Long customerSupplyOrderId;
    private Date createdAt;
    private Long quantity;
    private Float unitPrice;
    private Double totalPrice;
    private Float institutionDiscount;
    private Float distributorDiscount;
    private Float vat;
    private Long generalPrice;
    private String dispatchNo;
    private String invoiceNo;
    private Date expirationDate;
    private String surplus;
    private Long stocks;
    private Long surplusQuantity;
    //private int status;
    private String supplyOrderNo;
    private Float supplierProfit;
    private Long supervisorId;
    private Long totality;
    private Long totalQuantity;
    private Float averageUnitPrice;
    private Long drugCardId;//eklenecek
    private Float producerDiscount;
   // private Long orderDrugs;
  //-  private Long customerOrderDrugId;
    private Long purchaseOrderDrugs;
    private Long supplierId;//eklenecek

    private String note;
    private SupplyOrderPrice supplyOrderPrice;
    private Long otherCompanyId;
/*
    public CustomerSupplyOrderDto(CustomerSupplyOrder customerSupplyOrder) {
        this.customerSupplyOrderId =customerSupplyOrder.getCustomerSupplyOrderId();
        this.createdAt = customerSupplyOrder.getCreatedAt();
        this.quantity = customerSupplyOrder.getQuantity();
        this.unitPrice = customerSupplyOrder.getUnitPrice();
        this.totalPrice = customerSupplyOrder.getTotalPrice();
        this.institutionDiscount =customerSupplyOrder.getInstitutionDiscount();
        this.generalDiscount = customerSupplyOrder.getGeneralDiscount();
        this.vat = customerSupplyOrder.getVat();
        this.generalPrice = customerSupplyOrder.getGeneralPrice();
        this.dispatchNo = customerSupplyOrder.getDispatchNo();
        this.invoiceNo = customerSupplyOrder.getInvoiceNo();
        this.expirationDate = customerSupplyOrder.getExpirationDate();
       // this.status = customerSupplyOrder.getStatus();
       // this.supplyOrderNo = customerSupplyOrder.getSupplyOrderNo();
       // this.supervisorId = customerSupplyOrder.getSupervisorId();
        this.drugCard = customerSupplyOrder.getDrugCard().getDrugCardId();
        this.orderDrugs = customerSupplyOrder.getOrderDrugs().getCustomerOrderDrugId();
        this.supplier = customerSupplyOrder.getSupplier().getSupplierId();
    }*/
    public CustomerSupplyOrderDto() { }

}
