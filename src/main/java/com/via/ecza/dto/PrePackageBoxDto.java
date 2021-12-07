package com.via.ecza.dto;

import lombok.Data;

import javax.persistence.Lob;
import java.util.Date;

@Data
public class PrePackageBoxDto {

    private Long prePackageId;
    @Lob
    private String drugName;
    private Long drugBarcode;
    private Long drugSerialNo;
    private Date drugExpirationDate;
    private String drugLotNo;
    private String drugItsNo;
    private PackagingCustomerOrderDto customerOrder;
    private DepotCustomerSupplierOrderListDto customerSupplyOrder;

    public PrePackageBoxDto(){

    }
}
