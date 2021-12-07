package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CustomerOrderDrugDeleteDto {
    private Long customerOrderDrugId;
    private Long customerOrderId;
    private Long customerId;
    private Long preCustomerOrderDrugId;
}
