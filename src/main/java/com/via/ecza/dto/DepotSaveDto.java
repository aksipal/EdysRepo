package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Getter
@Setter
public class DepotSaveDto {

    private String drugName;
    private String drugBarcode;
    private String lotNo;
    private String serialNumber;
    private String customerOrderNo;
    private String supplierOrderNo;
    private Date stt;
    private String position;
    private String note;
    private String itsNo;
    public DepotSaveDto(){

    }
}
