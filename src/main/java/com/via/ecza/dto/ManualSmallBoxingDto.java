package com.via.ecza.dto;

import lombok.Data;

@Data
public class ManualSmallBoxingDto {
    private Long customerOrderId;
    private Long customerOrderDrugId;
    private Integer quantity;
    private Long drugCardId;
    private Long smallBoxId;
}
