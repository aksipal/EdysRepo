package com.via.ecza.dto;

import com.sun.istack.NotNull;
import com.via.ecza.entity.CurrencyType;
import com.via.ecza.entity.DrugCard;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.Date;

@Data
public class CampaignDto {


    private Long campaignId;
    private Date campaignStartDate;
    private Date campaignEndDate;
    private Date createdDate;
    private Double campaignUnitPrice;
    private Double campaignUnitCost;
    private DrugCard drugCard;
    private CurrencyType currencyType;
    private Double instutionDiscount;
    private Double campaignUnitPriceCurrency;

    private Double currencyFee;

    private Double campaignUnitPriceExcludingVat;

    private Double depotSalePriceExcludingVat;

    private int mf1;
    private int mf2;
    private int profit;

    private Double vat;
    public CampaignDto(){

    }
}
