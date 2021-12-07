package com.via.ecza.dto;

import com.via.ecza.entity.Category;
import com.via.ecza.entity.enumClass.CheckingCardType;
import com.via.ecza.entity.User;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Lob;
import java.util.Date;

@Data
@Setter
@Getter
public class CheckingCardDto {

    private Long checkingCardId;
    private String checkingCardName;
    private CountryDto country;
    private String city;
    private String salesRepresentative;
    private CheckingCardType type;
    @Lob
    private String address;
    private String taxOffice;
    private Long taxIdentificationNumber;
    private String phoneNumber;
    private String faxNumber;
    private String email;
    private User user;
    private Date createdAt;
    private String crsNo;
    private Long customerId;
    private Long companyId;
    private Category category;

    public CheckingCardDto(){

    }

}
