package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class SupplyDrugCardDto {

    private Long drugCardId;
    private String drugName;
    private SupplyPriceDto price;
}
