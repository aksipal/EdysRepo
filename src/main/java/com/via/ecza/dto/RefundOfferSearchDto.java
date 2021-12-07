package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class RefundOfferSearchDto {
    private Long drugCard;
    private Long supplier;
    private Long refundOfferStatus;
}
