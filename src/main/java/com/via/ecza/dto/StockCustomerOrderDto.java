package com.via.ecza.dto;

import com.via.ecza.entity.Country;
import com.via.ecza.entity.CustomerOrderStatus;
import com.via.ecza.entity.User;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Data
public class StockCustomerOrderDto {

    private Long customerOrderId;
    private String customerOrderNo;
    private Date orderDate;
    private Long userId;
    private Long orderStatusId;
    private User user;
    private CustomerOrderStatus orderStatus;
    private Country country;
    private CustomerDto customer;
    private int status;

    public StockCustomerOrderDto(){

    }
}
