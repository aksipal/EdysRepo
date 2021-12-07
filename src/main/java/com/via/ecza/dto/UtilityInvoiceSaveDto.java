package com.via.ecza.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class UtilityInvoiceSaveDto {

    //Fatura Eden
    private Long checkingCardId;
    //Fatura Edilen
    private Long otherCheckingCardId;
    //Fatura Amacı(Alım Satım)
    private String invoicePurpose;
    //Fatura Tipi ID'si ticari-hizmet-tevkifat
    private Long invoiceType;
    //İade True False
    private Boolean refund;

    @NotNull(message = " Boş Değer Olamaz")
    private String invoiceNo;

    @NotNull(message = " Boş Değer Olamaz")
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date invoiceDate;

    private Long otherCompanyId;
}
