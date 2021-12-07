package com.via.ecza.dto;

import lombok.Data;

@Data
public class CampaignSearchDto {

    private Long drugCardId;
    private Double campaignUnitPriceCurrency;

    public CampaignSearchDto(){

    }
}
