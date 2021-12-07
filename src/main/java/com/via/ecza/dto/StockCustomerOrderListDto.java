package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class StockCustomerOrderListDto {

    private Long customerOrderId;
    private String customerOrderNo;

    public StockCustomerOrderListDto(){

    }
}
