package com.via.ecza.dto;


import lombok.Data;

import java.util.Date;

@Data
public class CustomerSupplyOrderDrugSellDto {
    private Long supplierId;
    private Date startDate;
    private Date endDate;
    private String refundOrderNo;

}
