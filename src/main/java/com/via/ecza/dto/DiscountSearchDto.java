package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class DiscountSearchDto {


    private Long drugCardId;
    private Long drugCode;
    private float generalDiscount;
    private float instutionDiscount;
    private String surplusDiscount;

    public DiscountSearchDto() {
    }


}
