package com.via.ecza.dto;

import com.via.ecza.entity.CustomerSupplyOrder;
import com.via.ecza.entity.SupplierSupervisor;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.OneToMany;
import java.util.List;

@Data
public class AcceptanceSupplierSingleDto {

    private Long supplierId;
    private String supplierName;
    private String supplierCity;
    private String supplierDistrict;
    private String supplierAddress;
    private String supplierEmail;
    private String phoneNumber;
    private String supplierFax;
    private Float supplierProfit;
    private Integer status;

}
