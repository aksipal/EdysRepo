package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Getter
@Setter
public class SearchCustomerOrderDto {
    private String name;
    private String surname;
    private Long orderStatusId;
    private Long countryId;
    private String customerOrderNo;
    private Long companyId;
    private Date orderDate;
    private Long customerId;
    private Long city;
    private Long userId;
}
