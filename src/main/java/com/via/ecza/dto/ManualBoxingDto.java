package com.via.ecza.dto;

import lombok.Data;

@Data
public class ManualBoxingDto {
    private Long customerOrderId;
    private Integer quantity;
    private Long drugCardId;
    private Long boxId;
    private Long customerOrderDrugId;
}
