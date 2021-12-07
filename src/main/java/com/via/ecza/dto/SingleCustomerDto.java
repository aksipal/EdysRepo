package com.via.ecza.dto;

import com.via.ecza.entity.Country;
import com.via.ecza.entity.User;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Data
public class SingleCustomerDto {
    private Long customerId;
    private String name;
    private String surname;
    private Date createdDate;
    private Long countryId;
    private String city;
    private String jobTitle;
    private String eposta;
    private String businessPhone;
    private String mobilePhone;
    private String postalCode;
    private String customerFax;
    private Long companyId;
    private Long userId;
    private SingleCompanyDto company;
    private CountryDto country;
    private UserDto user;
    private int status;
    private String openAddress;

    public SingleCustomerDto(){

    }


}
