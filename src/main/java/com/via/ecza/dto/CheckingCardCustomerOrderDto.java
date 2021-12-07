package com.via.ecza.dto;

import com.via.ecza.entity.CurrencyType;
import com.via.ecza.entity.CustomerOrderStatus;
import lombok.Data;

@Data
public class CheckingCardCustomerOrderDto {

    private String customerOrderNo;
    private Long customerOrderId;
    private Double currencyFee;
    private CurrencyType currencyType;
    private CustomerOrderStatus orderStatus;
}
