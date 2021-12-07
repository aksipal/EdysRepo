package com.via.ecza.dto;

import com.via.ecza.entity.Country;
import com.via.ecza.entity.Customer;
import com.via.ecza.entity.User;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
@Data
public class CustomerDto {

    private String name;
    private String surname;
    private Date createdDate;
    private Long countryId;
    private String city;
    private String jobTitle;
    private String eposta;
    private String businessPhone;
    private String mobilePhone;
    private String mobilePhoneExtra;
    private String postalCode;
    private String customerFax;
    private Long companyId;
    private Long userId;
    private Country country;
    private User user;
    private int status;
    private String openAddress;
    private Long customerId;
    private SingleCompanyDto company;
    private String companyName;
    
    public CustomerDto(){

    }


    public CustomerDto( Customer customer){
        this.customerId=customer.getCustomerId();
        this.name=customer.getName();
        this.surname=customer.getSurname();
        this.createdDate=customer.getCreatedDate();
        this.city=customer.getCity();
        if(customer.getCountry() != null) this.countryId=customer.getCountry().getCountryId();
        this.jobTitle=customer.getJobTitle();
        this.eposta=customer.getEposta();
        this.businessPhone=customer.getBusinessPhone();
        this.mobilePhone=customer.getMobilePhone();
        this.postalCode=customer.getPostalCode();
        this.customerFax=customer.getCustomerFax();
        if(customer.getUser() != null) this.userId=customer.getUser().getUserId();
        this.country = customer.getCountry();
        this.status = customer.getStatus();
        this.user= customer.getUser();
        this.openAddress= customer.getOpenAddress();
        if(customer.getCompany() != null){
            if(customer.getCompany().getCompanyName() != null){
                this.companyName = customer.getCompany().getCompanyName();
            }
        }
    }
}
