package com.via.ecza.dto;

import com.via.ecza.entity.CustomerSupplyStatus;
import com.via.ecza.entity.DrugCard;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class DepotCustomerSupplierOrderListDto {

    private Long customerSupplyOrderId;
    private String supplyOrderNo;
    private Long chargedQuantity;
    private Long totality;
    private AcceptanceSupplierSingleDto supplier;
    private DrugCard drugCard;
    private CustomerSupplyStatus status;
    //private DepotCustomerOrderDrugs  customerOrderDrugs;
    private DepotPurchaseOrderDrugsDto purchaseOrderDrugs;
    private String supplierName;

    public DepotCustomerSupplierOrderListDto() {
    }
}
