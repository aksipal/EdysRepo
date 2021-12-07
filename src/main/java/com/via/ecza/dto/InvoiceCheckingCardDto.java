package com.via.ecza.dto;

import com.via.ecza.entity.*;
import com.via.ecza.entity.enumClass.CheckingCardType;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class InvoiceCheckingCardDto {

    private Long checkingCardId;
    private String checkingCardName;
    private List<InvoiceAccountDto> accounts;
    private Country country;
    private String city;
    private String salesRepresentative;
    private CheckingCardType type;
    private String address;
    private String taxOffice;
    private Long taxIdentificationNumber;
    private String phoneNumber;
    private String faxNumber;
    private String email;
    private Date createdAt;
    private User user;
    private Long companyId;
    private Long customerId;
    private Long supplierId;
    private String crsNo;
}
