package com.via.ecza.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;


import java.util.List;

@Getter
@Setter
@Data
public class SupplyOrdersDto {
    private Long customerOrderId;
    private int status;
    private CustomerDto customer;
    private List<SupplyCustomerDrugsDto> purchaseOrderDrugs;
}
