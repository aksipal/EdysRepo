package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Data
public class CustomerOrderAccountingSearchDto {
    private String customerOrderNo;
    private long countryId;
    private String city;
    private long customerId;


}
