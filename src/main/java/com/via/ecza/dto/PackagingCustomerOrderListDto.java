package com.via.ecza.dto;

import com.via.ecza.entity.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Setter
@Getter
public class PackagingCustomerOrderListDto {
    private Long customerOrderId;
    private Date orderDate;
    private Long userId;
    private Long orderStatusId;
    private String customerOrderNo;
    private User user;
    private CustomerOrderStatus orderStatus;
    private CustomerDto customer;
    private int status;
    private CurrencyType currencyType;
    private Double currencyFee;
    public PackagingCustomerOrderListDto(){

    }

    public PackagingCustomerOrderListDto(CustomerOrder order){
        this.customerOrderId = order.getCustomerOrderId();
        this.orderDate = order.getOrderDate();
        this.currencyType = order.getCurrencyType();
        this.currencyFee = order.getCurrencyFee();
        customerOrderNo = order.getCustomerOrderNo();
        orderStatus = order.getOrderStatus();
        customer = new CustomerDto(order.getCustomer());
    }


}
