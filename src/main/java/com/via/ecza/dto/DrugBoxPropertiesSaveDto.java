package com.via.ecza.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;


@Data
public class DrugBoxPropertiesSaveDto {

    private Long drugCardId;
    @NotNull(message = "Kutu Ağırlığı Boş Olamaz")
    private Double drugBoxWeight;
    @NotNull(message = "Kutu Eni Boş Olamaz")
    private Double drugBoxWidth;
    @NotNull(message = "Kutu Boyu Boş Olamaz")
    private Double drugBoxLength;
    @NotNull(message = "Kutu Yüksekliği Boş Olamaz")
    private Double drugBoxHeight;
}
