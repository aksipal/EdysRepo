package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class CheckingCardSearchDto {


    private String checkingCardName;
    private Long countryId;
    private String city;
    private String type;
    private String taxOffice;
    private Long taxIdentificationNumber;
    private Long customerId;
    private Long companyId;
    private Long checkingCardId;
    private Long otherCompanyId;

    public CheckingCardSearchDto(){

    }



}
