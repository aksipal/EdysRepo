package com.via.ecza.dto;

import com.via.ecza.entity.DrugCard;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Data
public class PriceDto {



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


    public PriceDto() {
    }

    public PriceDto(DrugCard drugCard, Long drugBarcode, Double realSourcePrice, Double realSourcePriceForCalculation, Double sourcePrice, String sourceCountryForCalculation, Integer salePriceType, Double salePriceToDepotExcludingVat, Double depotSalePriceExcludingVat, Double pharmacistSalePriceExcludingVat, Double retailSalePriceIncludingVat, Date dateOfChange, Date validityDate) {
        this.drugCard = drugCard;
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
