package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class StockCustomerSupplierOrderListDto {

    private Long customerSupplyOrderId;
    private String supplyOrderNo;
    private String supplierName;


    public StockCustomerSupplierOrderListDto() {
    }
}
