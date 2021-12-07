package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Data
public class CustomerSaveDto implements Serializable {
    private Long customerId;
    @NotNull
    @NotEmpty
    @Size(min=2, max=500)
//    @Pattern(regexp="(([A-Za-zğüşöçıİĞÜŞÖÇ]{3,30})|([A-Za-zğüşöçıİĞÜŞÖÇ]{2}[A-Za-zğüşöçıİĞÜŞÖÇ\\s]{1,}[A-Za-zğüşöçıİĞÜŞÖÇ]{1}))",message = "Lütfen Özel Karakter Girmeyiniz")
    private String name;
    @NotNull
    @NotEmpty
    @Size(min=2, max=500)
//    @Pattern(regexp="(([A-Za-zğüşöçıİĞÜŞÖÇ]{3,30})|([A-Za-zğüşöçıİĞÜŞÖÇ]{2}[A-Za-zğüşöçıİĞÜŞÖÇ\\s]{1,}[A-Za-zğüşöçıİĞÜŞÖÇ]{1}))",message = "Lütfen Özel Karakter Girmeyiniz")
    private String surname;
    private Date createdDate;
    private Long countryId;
    private String city;
    private String jobTitle;
    private String openAddress;

//    @Pattern(regexp="[A-Za-z]{1}[A-Za-z0-9!#$%&'*+/=?^_`{|}~.,-]{0,20}[A-Za-z0-9]{1}[@][A-Za-z0-9]{1,20}[.][A-Za-z]{1,20}",message = "Lütfen Email Adresini Doğru Girin")
    private String eposta;

//    @Pattern(regexp="[+]([1-9]{1}|[1-9]{1}[0-9]{1}|[1-9]{1}[0-9]{2})[-]{1}[1-9]{1}[0-9]{9}",message = "Lütfen +(Ülke Kodu)-(Telefon Numarası) Şeklinde Girin")
    private String businessPhone;
    @NotNull
    @NotEmpty
//    @Pattern(regexp="[+]([1-9]{1}|[1-9]{1}[0-9]{1}|[1-9]{1}[0-9]{2})[-]{1}[1-9]{1}[0-9]{9}",message = "Lütfen +(Ülke Kodu)-(Telefon Numarası) Şeklinde Girin")
    private String mobilePhone;
    private String mobilePhoneExtra;
    private String postalCode;
    private String customerFax;
    private Long companyId;
    private Long userId;
    public CustomerSaveDto(){

    }

}
