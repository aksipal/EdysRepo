package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Lob;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Getter
@Setter
@Data
public class DrugCardAndPriceDto {


    private Long drugCardId;
    @NotEmpty(message="İlaç Adı Boş Olamaz.")
    @NotNull(message="İlaç Adı Boş Olamaz.")
    @Lob
    private String drugName;
    @NotNull(message="İlaç Barkodu Boş Olamaz.")
    private Long drugCode;
    @NotEmpty(message="Firma Boş Olamaz.")
    @NotNull(message="Firma Boş Olamaz.")
    @Lob
    private String drugCompany;
//    @NotEmpty(message="Kaynak Ülke Boş Olamaz.")
//    @NotNull(message="Kaynak Ülke Boş Olamaz.")
    private String sourceCountry;
    private int status;
    @Positive(message = "Depocuya Satış Fiyatı Boş ve Negatif Olamaz.")
    private float salePriceToDepotExcludingVat;
    @Positive(message = "Depocu Satış Fiyatı Boş ve Negatif Olamaz.")
    private float depotSalePriceExcludingVat;
    @PositiveOrZero(message = "Eczacı Satış Fiyatı Negatif Olamaz.")
    private float pharmacistSalePriceExcludingVat;
    @PositiveOrZero(message = "Perakende Satış Fiyatı Negatif Olamaz.")
    private float retailSalePriceIncludingVat;
    @NotNull(message="Kdv Oranı Boş veya Negatif Olamaz.")
    @PositiveOrZero(message = "Kdv Oranı Negatif Olamaz.")
    private Double drugVat;
    @NotEmpty(message="Firma Boş Olamaz.")
    @NotNull(message="Firma Boş Olamaz.")
    private String sourceCountryForCalculation;

    /*KDV Hariç Depocuya Satış TL Fiyatı (₺)---------------sale_price_to_depot_excluding_vat
    KDV Hariç Depocu Satış TL Fiyatı (₺)-----------------depot_sale_price_excluding_vat
    KDV Hariç Eczacı Satış TL Fiyatı (₺)-----------------pharmacist_sale_price_excluding_vat
    KDV Dahil Perakende Satış TL Fiyatı (₺)--------------retail_sale_price_including_vat*/

    public DrugCardAndPriceDto() {
    }


}
