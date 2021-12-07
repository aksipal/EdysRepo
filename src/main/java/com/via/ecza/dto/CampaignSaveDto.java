package com.via.ecza.dto;

import com.via.ecza.entity.CurrencyType;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.*;
import java.util.Date;

@Data
public class CampaignSaveDto {

    @NotNull
    private Long drugCardId;
    private Long campaignId;
    @NotNull
    //@FutureOrPresent(message = "Kampanya Başlangıç Tarihi Bugünden sonra olmalıdır.")
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    //@Future(message = "Kampanya Başlangıç Tarihi Belirleyiniz.")
    private Date campaignStartDate;
    @NotNull
    @Future(message = "Kampanya Bitiş Tarihi Belirleyiniz.")
    private Date campaignEndDate;
    //@NotNull
    @Positive(message = "Kampanyalı Fiyat Boş ve Negatif Olamaz.")
    private Double campaignUnitPrice;
    private Double campaignUnitCost;
    private Date createdDate;

    @PositiveOrZero
    private int mf1;

    @PositiveOrZero
    private int mf2;
    @PositiveOrZero
    private int profit;
    @NotNull
    private Double currencyFee;
    @NotNull
    private Double campaignUnitPriceExcludingVat;
    private Double depotSalePriceExcludingVat;
    private int vat;
    private Double campaignUnitPriceCurrency;
    private Double instutionDiscount;
    private CurrencyType currencyType;

    public CampaignSaveDto(){

    }
}
