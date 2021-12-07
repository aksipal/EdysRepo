package com.via.ecza.dto;

import com.via.ecza.entity.CurrencyType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;


@Getter
@Setter
@Data
public class CustomerOrderSearchDto {
    private String customerOrderNo;
    private long countryId;
    private String city;
    private long orderStatusId;
    private long customerId;
    private CurrencyType currencyType;
    private Double currencyFee;

}
