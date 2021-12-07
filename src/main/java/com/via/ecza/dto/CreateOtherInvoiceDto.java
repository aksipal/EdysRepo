package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.util.Date;

@Data
@Setter
@Getter
public class CreateOtherInvoiceDto {

    private Long checkingCardId;
    private Long otherCheckingCardId;
    @NotNull(message = "Kâr Payı Boş Olamaz.")
    @PositiveOrZero(message = "Kâr Payı Negatif ve Sıfır Olamaz.")
    private Double profit;
    //başkasına fatura edilecek olan fatura
    private Long invoiceId;
    @NotNull(message = "Fatura No Boş Olamaz.")
    @NotEmpty(message = "Fatura No Boş Olamaz.")
    private String invoiceNo;
    @NotNull(message = "Fatura Tarihi Boş Olamaz.")
    private Date invoiceDate;

    public CreateOtherInvoiceDto() {

    }

}
