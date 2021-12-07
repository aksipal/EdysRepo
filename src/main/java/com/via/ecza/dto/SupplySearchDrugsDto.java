package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class SupplySearchDrugsDto {

    private Long customerOrderId;
    private Long drugCard;
    private String purchaseStatusId;
}
