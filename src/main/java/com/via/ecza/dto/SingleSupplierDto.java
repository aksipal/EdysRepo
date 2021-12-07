package com.via.ecza.dto;

import com.via.ecza.entity.Supplier;
import com.via.ecza.entity.SupplierType;
import com.via.ecza.entity.User;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter

public class SingleSupplierDto {

    private Long supplierId;
    private String supplierName;
    private String supplierCity;
    private String supplierDistrict;
    private String supplierAddress;
    private String supplierEmail;
    private String phoneNumber;
    private String supplierFax;
    private String supplierTaxNo;
    private Float supplierProfit;
    private String supplierType;
    private UserDto user;
    private int status;
    private List<SearchSupplierSupervisorDto> supplierSupervisors;

    public SingleSupplierDto(Supplier supplier) {
        this.supplierId = supplier.getSupplierId();
        this.supplierName = supplier.getSupplierName();
        this.supplierCity = supplier.getSupplierCity();
        this.supplierDistrict = supplier.getSupplierDistrict();
        this.supplierAddress = supplier.getSupplierAddress();
        this.supplierEmail = supplier.getSupplierEmail();
        this.phoneNumber = supplier.getPhoneNumber();
        this.supplierFax = supplier.getSupplierFax();
        this.status = supplier.getStatus();
    }

    public SingleSupplierDto() {
    }
}
