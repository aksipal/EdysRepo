package com.via.ecza.dto;

import lombok.Data;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Date;

@Data
public class CheckingCardInvoiceDto {

    private Long checkingCardId;
    private String customerOrderNo;
    private Long customerOrderId;
    @NotNull
    @Positive(message = "Birim Fiyat Boş ve Negatif Olamaz.")
    private Double instantCurrencyFee;
//    @NotNull
//    private String crsNo;
    @NotNull
    @Positive(message = "Birim Fiyat Boş ve Negatif Olamaz.")
    private Double freightCostCurrency;
    @NotNull
    @FutureOrPresent
    private Date paymentTerm;
    @NotNull
    private Date invoiceDate;
    @NotNull
    private String invoiceNo;
    @NotEmpty
    @NotNull
    private String totalPriceExpression;
    @NotEmpty
    @NotNull
    private String totalPriceCurrencyExpression;

    private Long otherCompanyId;

}
