package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Setter
@Getter
public class SearchCustomerDto {
    private String name;
    private String surname;
    private Date createdDate;
    private Long countryId;
    private String city;
    private Long companyId;
    private Long userId;
}
