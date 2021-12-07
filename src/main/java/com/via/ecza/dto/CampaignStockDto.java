package com.via.ecza.dto;

import com.via.ecza.entity.CurrencyType;
import com.via.ecza.entity.DrugCard;
import lombok.Data;

import java.math.BigInteger;
import java.util.Date;

@Data
public class CampaignStockDto {


    private BigInteger drugCardId;
    private BigInteger count;
    private String drugName;
    private String drugBarcode;
    private DrugCard drugCard;

    public CampaignStockDto(){

    }
}
