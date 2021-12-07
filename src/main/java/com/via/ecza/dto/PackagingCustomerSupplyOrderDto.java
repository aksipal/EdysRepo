package com.via.ecza.dto;


import com.via.ecza.entity.CustomerSupplyOrder;
import com.via.ecza.entity.DrugCard;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Data
public class PackagingCustomerSupplyOrderDto {

    private Long customerSupplyOrderId;
    private Date createdAt;
    private Long quantity;
    private Float averageUnitPrice;
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
    private Long supervisorId;
    private String supplyOrderNo;


    private PackagingSupplierDto supplier;
    private PackagingPurchaseOrderDrugsDto purchaseOrderDrugs;
//    private PackagingCustomerOrderDrugsDto customerOrderDrugs;

    public PackagingCustomerSupplyOrderDto() {

    }
    public PackagingCustomerSupplyOrderDto(CustomerSupplyOrder customerSupplyOrder) {

        this.customerSupplyOrderId = customerSupplyOrder.getCustomerSupplyOrderId();
        this.createdAt = customerSupplyOrder.getCreatedAt();
        this.quantity = customerSupplyOrder.getQuantity();
        this.averageUnitPrice = customerSupplyOrder.getAverageUnitPrice();
        this.totalPrice = customerSupplyOrder.getTotalPrice();
        this.institutionDiscount = customerSupplyOrder.getInstitutionDiscount();
        this.distributorDiscount = customerSupplyOrder.getDistributorDiscount();
        this.vat = customerSupplyOrder.getVat();
        this.generalPrice = customerSupplyOrder.getGeneralPrice();
        this.dispatchNo = customerSupplyOrder.getDispatchNo();
        this.surplus = customerSupplyOrder.getSurplus();
        this.surplusQuantity = customerSupplyOrder.getSurplusQuantity();
        this.note = customerSupplyOrder.getNote();
        this.totality = customerSupplyOrder.getTotality();
        this.supplierProfit = customerSupplyOrder.getSupplierProfit();
        this.stocks = customerSupplyOrder.getStocks();
       // this.status = customerSupplyOrder.getStatus();
        this.supervisorId = customerSupplyOrder.getSupervisorId();
        this.supplyOrderNo = customerSupplyOrder.getSupplyOrderNo();


        //customerOrderDrugs = new PackagingCustomerOrderDrugsDto(customerSupplyOrder.getCustomerOrderDrugs());
        purchaseOrderDrugs = new PackagingPurchaseOrderDrugsDto(customerSupplyOrder.getPurchaseOrderDrugs());
        supplier = new PackagingSupplierDto(customerSupplyOrder.getSupplier());
    }

}
