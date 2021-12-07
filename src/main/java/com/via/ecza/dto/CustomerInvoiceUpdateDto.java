package com.via.ecza.dto;

import lombok.Data;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Date;

@Data
public class CustomerInvoiceUpdateDto {

    private Long checkingCardId;
    private String customerOrderNo;
    private Long customerOrderId;
    private Long invoiceId;
    @NotNull
    @Positive(message = "Birim Fiyat Boş ve Negatif Olamaz.")
    private Double instantCurrencyFee;
    @NotNull
    private String crsNo;
    @NotNull
    @Positive(message = "Birim Fiyat Boş ve Negatif Olamaz.")
    private Double freightCostCurrency;
    @NotNull
    @FutureOrPresent
    private Date paymentTerm;
    @NotEmpty
    @NotNull
    private String totalPriceExpression;
    @NotEmpty
    @NotNull
    private String totalPriceCurrencyExpression;

}
