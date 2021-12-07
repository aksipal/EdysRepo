package com.via.ecza.dto;


import com.via.ecza.entity.Country;
import com.via.ecza.entity.CurrencyType;
import com.via.ecza.entity.CustomerOrderStatus;
import com.via.ecza.entity.User;
import lombok.Data;

import java.util.Date;

@Data
public class CustomerOrderSearchListDto {


    private Long customerOrderId;
    private Date orderDate;
    private Long userId;
    private Long orderStatusId;
    private String customerOrderNo;
    private User user;
    private CustomerOrderStatus orderStatus;
    private Country country;
    private SingleCustomerDto customer;
    private int status;
    private CurrencyType currencyType;
    private SingleCompanyDto company;
    private Double currencyFee;
}
