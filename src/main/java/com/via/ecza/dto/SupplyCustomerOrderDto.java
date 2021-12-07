package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class SupplyCustomerOrderDto {
    private Long customerOrderId;
    private String customerOrderNo;
    private SupplyCustomerDto customerId;
}
