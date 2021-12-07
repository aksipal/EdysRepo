package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class PharmacyIndexDto {
    private Long supplyOfferCount;
    private Long supplyOrderCount;
    private Long refundOfferCount;
    private Long refundCount;
}
