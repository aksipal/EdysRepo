package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class SupplyPriceDto {
    private Long priceId;
    private Double depotSalePriceExcludingVat;
    private Double salePriceToDepotExcludingVat;
}
