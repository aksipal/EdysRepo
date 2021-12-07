package com.via.ecza.dto;

import com.via.ecza.entity.CurrencyType;
import com.via.ecza.entity.CustomerOrderStatus;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Lob;
import java.util.Date;

@Data
@Setter
@Getter
public class CustomerOrderForExporterDto {

    private Long customerOrderId;
    private String customerOrderNo;
    private Date orderDate;
    //private CustomerDto customer;
    private SingleCustomerDto customer;
    private SingleCompanyDto company;
    private CustomerOrderStatus orderStatus;
    private CurrencyType currencyType;
    private Double currencyFee;
    @Lob
    private String customerOrderNote;

    public CustomerOrderForExporterDto(){

    }
}
