package com.via.ecza.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;


@Data
@Getter
@Setter
public class SupplyDepotOfferDto {
    private Date expirationDate;
    private Long drugCard;
    private Long limitation;
    private Long customerSupplyOrder;
    private Long customerOrder;

    public SupplyDepotOfferDto() {
    }
}
