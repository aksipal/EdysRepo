package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@Getter
@Setter
public class StockSaveDto {

    private String drugName;
    private Long drugCardId;
    private String drugBarcode;
    private Long drugCode;
    private Date expirationDate;
    private String customerOrderNo;
    private String lotNo;
    private String serialNumber;
    private String supplierOrderNo;
    private Long customerOrderId;
    private String itsNo;
    private String position;
    private String note;

    public StockSaveDto(){

    }
}
