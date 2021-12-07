package com.via.ecza.dto;

import lombok.Data;

@Data
public class OrderQuantitiesByMonthDto {
    private int monthValue;
    private Long drugCount;
}
