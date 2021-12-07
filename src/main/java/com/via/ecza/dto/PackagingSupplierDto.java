package com.via.ecza.dto;

import com.sun.istack.NotNull;
import com.via.ecza.entity.Supplier;
import lombok.Data;

import javax.persistence.Column;
import javax.validation.constraints.NotEmpty;

@Data
public class PackagingSupplierDto {

    private Long supplierId;
    private String supplierName;
    private String supplierCity;
    private String supplierDistrict;
    private String supplierAddress;
    private String supplierEmail;
    private String phoneNumber;
    private String supplierFax;
    private Float supplierProfit;

    public PackagingSupplierDto(){

    }

    public PackagingSupplierDto(Supplier supplier){

        this.supplierId = supplier.getSupplierId();
        this.supplierName = supplier.getSupplierName();
        this.supplierCity = supplier.getSupplierCity();
        this.supplierDistrict = supplier.getSupplierDistrict();
        this.supplierAddress = supplier.getSupplierAddress();
        this.supplierEmail = supplier.getSupplierEmail();
        this.phoneNumber = supplier.getPhoneNumber();
        this.supplierFax = supplier.getSupplierFax();
        this.supplierProfit = supplier.getSupplierProfit();

    }

}
