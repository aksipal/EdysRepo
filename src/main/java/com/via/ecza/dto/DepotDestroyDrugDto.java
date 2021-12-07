package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@Getter
@Setter
public class DepotDestroyDrugDto {

    private String drugName;
    private String drugBarcode;
    private String lotNo;
    private String serialNumber;
    private String customerOrderNo;
    private String supplierOrderNo;
    private Date stt;
    private String position;
    @NotNull(message="İlacın İmha Sebebi Belirtilmek Zorundadır.")
    @NotBlank(message="İlacın İmha Sebebi Belirtilmek Zorundadır.")
    private String note;
    private String itsNo;
    public DepotDestroyDrugDto(){

    }
}
