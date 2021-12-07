package com.via.ecza.dto;

import lombok.Data;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class SellInvoiceDto {

    @NotNull
    private String crsNo;
    @NotNull
    @FutureOrPresent
    private Date paymentTerm;
    @NotEmpty
    @NotNull
    private String totalPriceExpression;
}
