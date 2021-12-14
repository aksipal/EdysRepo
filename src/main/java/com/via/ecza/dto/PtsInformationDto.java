package com.via.ecza.dto;

import lombok.Data;

import java.util.Date;

@Data
public class PtsInformationDto {

    private Long ptsInformationId;
    private String boxBarcode;
    private String drugQrCode;
    private int status;
    private Date createdAt;


    public PtsInformationDto() {
    }

    public PtsInformationDto(Long ptsInformationId, String boxBarcode, String drugQrCode, int status, Date createdAt) {
        this.ptsInformationId = ptsInformationId;
        this.boxBarcode = boxBarcode;
        this.drugQrCode = drugQrCode;
        this.status = status;
        this.createdAt = createdAt;
    }
}
