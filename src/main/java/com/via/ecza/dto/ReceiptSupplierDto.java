package com.via.ecza.dto;

import com.via.ecza.entity.SupplierType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ReceiptSupplierDto {
    private Long supplierId;
    private String supplierName;
    private SupplierType supplierType;
    private Integer status;
    private String supplierCity;
    private String supplierDistrict;
}
