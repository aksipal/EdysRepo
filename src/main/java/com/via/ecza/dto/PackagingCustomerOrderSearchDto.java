package com.via.ecza.dto;

import lombok.Data;

@Data
public class PackagingCustomerOrderSearchDto {

    private String smallboxNo;
    private String customerOrderId;
    private String customerOrderNo;
    private Long drugCardId;
    private String sortCriteria;
    private String sortDirection;

}
