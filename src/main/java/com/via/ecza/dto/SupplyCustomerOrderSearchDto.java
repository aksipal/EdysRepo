package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;


@Getter
@Setter
@Data
public class SupplyCustomerOrderSearchDto {
    private Long customerId;
    private Long orderStatusId;
    private Long countryId;
    private Long drugCard;
    private Date expirationDate;
    private String customerOrderNo;
    private Long userId;
}
