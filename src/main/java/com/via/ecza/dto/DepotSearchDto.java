package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Data
public class DepotSearchDto {
    private Long drugCardId;
    private String drugCode;
    private String lotNo;
    private String serialNo;
    private String customerOrderNo;
    private String supplierOrderNo;
    private Date stt;
    private Long depotDrugStatus;
    private Long supplierId;
    private String boxNo;
    private String smallBoxNo;
    private String sortCriteria;
    private String sortDirection;

    public DepotSearchDto(){

    }


}
