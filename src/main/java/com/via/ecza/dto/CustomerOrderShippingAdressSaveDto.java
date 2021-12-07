package com.via.ecza.dto;

import com.via.ecza.entity.Country;
import lombok.Data;

import java.util.Date;

@Data
public class CustomerOrderShippingAdressSaveDto {

    private Long customerOrderId;
    private String companyName;
    private String contactName;
    private String fullAddress;
    private String city;
    private Long countryId;
    private String phone;
    private String email;
    private Date createdDate;

    public CustomerOrderShippingAdressSaveDto(){

    }
}
