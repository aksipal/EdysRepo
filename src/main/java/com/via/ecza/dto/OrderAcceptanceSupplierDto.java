package com.via.ecza.dto;

import com.sun.istack.NotNull;
import com.via.ecza.entity.*;
import lombok.Data;

@Data
public class OrderAcceptanceSupplierDto {

    private Long supplierId;
    private String supplierName;
    private String supplierCity;
    private String supplierDistrict;
    private String supplierAddress;
    private String supplierEmail;
    private String phoneNumber;
    private String supplierFax;
    private Float supplierProfit;
    private SupplierType supplierType;
    private User user;
    private Integer status;
}
