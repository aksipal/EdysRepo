package com.via.ecza.dto;


import com.via.ecza.entity.SupplierType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class SupplierSearchDto {
    private String supplierName;
    private String supplierCity;
    private String supplierDistrict;
    private String phoneNumber;
    private Float supplierProfit;
    private SupplierType supplierType;
}
