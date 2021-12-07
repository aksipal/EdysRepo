package com.via.ecza.dto;

import com.via.ecza.entity.CurrencyType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Positive;


@Data
@Getter
@Setter
public class CustomerOrderUpdateDto {
    private Long customerOrderId;
    private Long customerId;
    private CurrencyType currencyType;
    @Positive
    private Double currencyFee;
    private String paymentTerms;
    private String deliveryTerms;
    private String leadTime;
    private String additionalDetails;
    private Double freightCostTl;
}
