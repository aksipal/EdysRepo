package com.via.ecza.dto;

import com.via.ecza.entity.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Data
@Setter
@Getter
public class SingleExporterOrderDto {
    private Long customerOrderId;
    private Date orderDate;
    private String customerOrderNo;
    private User user;
    private CustomerOrderStatus orderStatus;
    private SingleCustomerDto customer;
    private int status;
    private String customerOrderNote;
    private List<CustomerOrderDrugsListDto> customerOrderDrugs;
    private SingleCompanyDto company;
    private CountryDto country;

    public SingleExporterOrderDto(){

    }
}
