package com.via.ecza.dto;


import lombok.Data;

import java.util.Date;

@Data
public class CustomerSupplyOrderDrugDto {
    private Long supplierId;
    private Date startDate;
    private Date endDate;
    private String supplierOrderNo;

}
