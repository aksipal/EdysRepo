package com.via.ecza.dto;

import com.via.ecza.entity.Country;
import com.via.ecza.entity.CurrencyType;
import com.via.ecza.entity.CustomerOrderStatus;
import com.via.ecza.entity.User;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Data
@Setter
@Getter
public class CustomerOrderAccountingDto {
    private Long customerOrderId;
    private Date orderDate;
    private Long userId;
    private Long orderStatusId;
    private String customerOrderNo;
    private User user;
    private CustomerOrderStatus orderStatus;
    private Country country;
    private CustomerDto customer;
    private int status;
    private CurrencyType currencyType;
    private Double currencyFee;
    private List<CustomerOrderDrugsDto> customerOrderDrugs;
    public CustomerOrderAccountingDto(){

    }

}
