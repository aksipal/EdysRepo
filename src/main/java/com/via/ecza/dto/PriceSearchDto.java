package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class PriceSearchDto {



    private Long drugCardId;
    private Long drugCode;
    private Double depotSalePriceExcludingVat;
    private Double depotSalePriceExcludingVatWithInstutionDiscount;


    public PriceSearchDto() {
    }

}
