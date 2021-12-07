package com.via.ecza.entity;

import lombok.Data;

import java.util.Date;

@Data
public class SinglePriceDto {

    private Long priceId;
    private DrugCard drugCard;
    private Long drugBarcode;
    private Double realSourcePrice;
    private Double realSourcePriceForCalculation;
    private Double sourcePrice;
    private String sourceCountryForCalculation;
    private Integer salePriceType;
    private Double salePriceToDepotExcludingVat;
    private Double depotSalePriceExcludingVat;
    private Double pharmacistSalePriceExcludingVat;
    private Double retailSalePriceIncludingVat;
    private Date dateOfChange;
    private Date validityDate;
    private int status;


    public SinglePriceDto() {
    }

    public SinglePriceDto(Long drugBarcode, Double realSourcePrice, Double realSourcePriceForCalculation, Double sourcePrice, String sourceCountryForCalculation, Integer salePriceType, Double salePriceToDepotExcludingVat, Double depotSalePriceExcludingVat, Double pharmacistSalePriceExcludingVat, Double retailSalePriceIncludingVat, Date dateOfChange, Date validityDate) {
        this.drugBarcode = drugBarcode;
        this.realSourcePrice = realSourcePrice;
        this.realSourcePriceForCalculation = realSourcePriceForCalculation;
        this.sourcePrice = sourcePrice;
        this.sourceCountryForCalculation = sourceCountryForCalculation;
        this.salePriceType = salePriceType;
        this.salePriceToDepotExcludingVat = salePriceToDepotExcludingVat;
        this.depotSalePriceExcludingVat = depotSalePriceExcludingVat;
        this.pharmacistSalePriceExcludingVat = pharmacistSalePriceExcludingVat;
        this.retailSalePriceIncludingVat = retailSalePriceIncludingVat;
        this.dateOfChange = dateOfChange;
        this.validityDate = validityDate;
    }
}
