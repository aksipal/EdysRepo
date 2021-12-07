package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Getter
@Setter
public class SupplyDrugsByDateDto {
    private Long supplier;
    private String supplierName;
    private Long count;
    private Long drugCard;
    private Date expirationDate;
    private Float averageUnitPrice;
    private Long boughtFrom;
    private String refundNote;
    private CustomerSupplyOrderDto customerSupplyOrder;
    private Long otherCompanyId;
}
