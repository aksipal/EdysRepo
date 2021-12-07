package com.via.ecza.dto;

import com.via.ecza.entity.*;
import com.via.ecza.entity.enumClass.CheckingCardType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Setter
@Getter
public class CheckingCardSaveDto {


    @NotNull(message="Cari Kart Adı Boş Olamaz.")
    @NotEmpty(message="Cari Kart Adı Boş Olamaz.")
    private String checkingCardName;
    private List<Account> accounts;
    private List<AccountActivity> accountActivities;
    @NotNull
    private Long countryId;
    @NotNull(message="Şehir Boş Olamaz.")
    @NotEmpty(message="Şehir Boş Olamaz.")
    private String city;
    private String salesRepresentative;
    @NotNull
    private CheckingCardType type;
    @Lob
    @NotNull(message="Adres Boş Olamaz.")
    @NotEmpty(message="Adres Boş Olamaz.")
    private String address;
    private String taxOffice;
    @NotNull(message="Vergi No Boş Olamaz.")
    private Long taxIdentificationNumber;
    private String phoneNumber;
    private String faxNumber;
    @Email(message = "Lütfen Geçerli E-Posta Giriniz.")
    private String email;
    private User user;
    private String crsNo;

    public CheckingCardSaveDto(){

    }



}
