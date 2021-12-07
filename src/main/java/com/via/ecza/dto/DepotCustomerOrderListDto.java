package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class DepotCustomerOrderListDto {
    private Long customerOrderId;
    private String customerOrderNo;
    private CustomerDto customer;
    public DepotCustomerOrderListDto(){

    }

}
