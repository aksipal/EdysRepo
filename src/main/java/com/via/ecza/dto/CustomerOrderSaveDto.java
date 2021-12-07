package com.via.ecza.dto;

import com.via.ecza.entity.CurrencyType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;


@Getter
@Setter
@Data
public class CustomerOrderSaveDto {
    private Long customerOrderId;
    private Long customerId;
    private CurrencyType currencyType;
    private Double currencyFee;
    private String customerOrderNote;


}
