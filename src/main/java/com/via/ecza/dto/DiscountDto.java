package com.via.ecza.dto;

import com.via.ecza.entity.DrugCard;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.*;

@Getter
@Setter
@Data
public class DiscountDto {

    private Long discountId;
    private DrugCard drugCard;
    @PositiveOrZero(message = "Ticari İskonto Negatif Olamaz.")
    private float generalDiscount;
    @PositiveOrZero(message = "Kurum İskonto Negatif Olamaz.")
    private float instutionDiscount;
    @NotNull(message="Mal Fazlası Boş Olamaz.")
    @NotEmpty(message="Mal Fazlası Boş Olamaz.")
    @Pattern(regexp="([1-9]{1}[0-9]{0,20}[+]{1}[1-9]{1}[0-9]{0,20})|([1-9]{1}[0-9]{0,20}[+]{1}[0]{1})",message = "Lütfen İstenen Formatı Sağlayın --> x+x , En Düşük 1+0 Girilebilir")
    private String surplusDiscount;

    public DiscountDto() {

    }

}
