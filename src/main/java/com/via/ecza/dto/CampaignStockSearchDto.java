package com.via.ecza.dto;

import lombok.Data;

@Data
public class CampaignStockSearchDto {

    private Long drugCardId;
    private Integer minCount;
    private Integer maxCount;

    public CampaignStockSearchDto(){

    }
}
