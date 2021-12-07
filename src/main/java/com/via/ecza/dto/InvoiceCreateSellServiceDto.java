package com.via.ecza.dto;


import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
public class InvoiceCreateSellServiceDto {

    @NotEmpty
    @NotNull
    private String categoryId;      // Mal Hizmet
    @NotNull
    @Positive(message = "Birim Fiyat Boş ve Negatif Olamaz.")
    private Double quantity;               // miktar
    @NotEmpty
    @NotNull
    private String unit;                // Birim
    private Double tagPrice;            // etiket fiyatı
    private Double wareHousemanPrice;   // depocu fiyatı
    @NotNull
    @Positive(message = "Birim Fiyat Boş ve Negatif Olamaz.")
    private Double unitPrice;           // birim fiyatı
    private int generalDiscount;        // kurum iskonto
    private int sellDiscount;           // satış iskonto
    private int advanceDiscount;        // satış iskonto
    @NotNull
    @Positive(message = "Net Fiyat Boş ve Negatif Olamaz.")
    private Double netPrice;            // net fiyat
    @NotNull
    @Positive(message = "KDV Boş ve Negatif Olamaz.")
    private Double vat;                 // kdv
    @NotNull
    @Positive(message = "KDV Tutarı Boş ve Negatif Olamaz.")
    private Double vatSum;              // kdv fiyatı
    @NotNull
    @Positive(message = "Toplam Fiyat Boş ve Negatif Olamaz.")
    private Double totalPrice;          // toplam fiyat
    private Double productServiceSum;

    private Long otherCompanyId;


}
