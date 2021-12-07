package com.via.ecza.dto;

import com.via.ecza.entity.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
@Setter
@Getter
public class CustomerOrderDto {

    private Long customerOrderId;
    private String customerOrderNo;
    private Date orderDate;
    //private CustomerDto customer;
    private SingleCustomerDto customer;
    private SingleCompanyDto company;
    private User user;
    private CustomerOrderStatus orderStatus;
    private CurrencyType currencyType;
    private Double currencyFee;
    @Lob
    private String customerOrderNote;

    public CustomerOrderDto(){

    }
}
