package com.via.ecza.dto;

import lombok.Data;

@Data
public class SingleDiscountDto {

    private Long discountId;
    private float generalDiscount;
    private float instutionDiscount;
    private String surplusDiscount;

}
