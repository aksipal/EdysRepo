package com.via.ecza.dto;

import lombok.Data;

@Data
public class BoxFakeDrugDto {

    private Long boxId;
    private int drugQuantity;
    private Long customerOrderId;
    private Long drugCardId;
}
