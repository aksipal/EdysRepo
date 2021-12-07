package com.via.ecza.dto;

import com.via.ecza.entity.Country;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Getter
@Setter
public class SingleCompanyDto {
    private Long companyId;

    private String companyName;

    private String taxNo;

    private String address;

    private String companyPhone;

    private String companyMobilePhone;

    private String companyFax;

    private String city;

    private Date createdDate;

    private CountryDto country;
    private String emailAddress;
    private int status;

}
