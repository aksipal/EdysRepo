package com.via.ecza.dto;

import com.via.ecza.entity.Country;
import com.via.ecza.entity.CustomerOrder;
import lombok.Data;

import javax.persistence.*;

@Data
public class CustomerOrderShippingAdressDto {

    private Long customerOrderShippingAdressId;
    private String companyName;
    private String contactName;
    private String fullAddress;
    private String city;
    private Country country;
    private String phone;
    private String email;
    private CustomerOrderDto customerOrder;

    public CustomerOrderShippingAdressDto() {

    }
}
