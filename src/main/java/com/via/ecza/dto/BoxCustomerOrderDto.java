package com.via.ecza.dto;

import com.via.ecza.entity.CurrencyType;
import lombok.Data;

import java.util.Date;

@Data
public class BoxCustomerOrderDto {
    private Long customerOrderId;
    private String customerOrderNo;
    private Date orderDate;
    private CurrencyType currencyType;
    private Double currencyFee;
    private String customerOrderNote;

    public BoxCustomerOrderDto(){

    }
}
