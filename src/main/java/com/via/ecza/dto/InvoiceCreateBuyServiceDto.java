package com.via.ecza.dto;


import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
public class InvoiceCreateBuyServiceDto {
    @NotEmpty
    @NotNull
    private String categoryId;
    @NotEmpty
    @NotNull
    private String productService;
    @NotNull
    @Positive(message = "Birim Fiyat Boş ve Negatif Olamaz.")
    private Double quantity;
    @NotEmpty
    @NotNull
    private String unit;
    @NotNull
    @Positive(message = "Birim Fiyat Boş ve Negatif Olamaz.")
    private Double unitPrice;
    private int discount;
    private Double discountSum;
    @NotNull
    @Positive(message = "KDV Boş ve Negatif Olamaz.")
    private Double vat;
    @NotNull
    @Positive(message = "KDV Tutarı Boş ve Negatif Olamaz.")
    private Double vatSum;
    @NotNull
    @Positive(message = "Mal Hizmet Tutarı Boş ve Negatif Olamaz.")
    private Double productServiceSum;
    @NotNull
    @Positive(message = "Ödenecek Tutar Boş ve Negatif Olamaz.")
    private Double totalPrice;

    private Long otherCompanyId;
}
