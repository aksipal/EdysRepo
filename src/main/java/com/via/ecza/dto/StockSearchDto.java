package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Data
public class StockSearchDto {

    private Long depotId;
    private String drugCode;
    private Date expirationDate;
    private String lotNo;
    private String itsNo;
    private String serialNumber;
    private String customerOrderNo;
    private String supplierOrderNo;
    private Long customerOrderId;
    private Long drugCardId;
    private Long supplierId;
    private String sortCriteria;
    private String sortDirection;

    public StockSearchDto(){

    }

}