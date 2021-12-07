package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class DiscountExcelDto {


    private Long drugCode;
    private float generalDiscount;
    private float instutionDiscount;
    private float instutionDiscount1;
    private float instutionDiscount2;
    private float instutionDiscount3;
    private float instutionDiscount4;
    private String surplusDiscount;
    private String passivationDate;

    public DiscountExcelDto() {

    }


}
