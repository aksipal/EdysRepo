package com.via.ecza.dto;


import com.sun.istack.NotNull;
import com.via.ecza.entity.SupplierType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.*;

@Data
@Getter
@Setter
public class SupplierSaveDto {

    private Long supplierId;

    @NotEmpty
    @NotNull
    @Size(min=3, max=30)
    @Pattern(regexp="(([A-Za-zğüşöçıİĞÜŞÖÇ]{3,30})|([A-Za-zğüşöçıİĞÜŞÖÇ]{2}[A-Za-zğüşöçıİĞÜŞÖÇ\\s]{1,}[A-Za-zğüşöçıİĞÜŞÖÇ]{1}))",message = "Lütfen Özel Karakter Girmeyiniz")
    private String supplierName;

    @NotEmpty
    @NotNull
    @Size(min=3, max=10)
    @Pattern(regexp="^[A-Za-zğüşöçıİĞÜŞÖÇ]+$",message = "Lütfen Özel Karakter, Boşluk veya Rakam Kullanmayınız")
    private String supplierCity;

    @NotEmpty
    @NotNull
    @Size(min=3, max=15)
    @Pattern(regexp="^[A-Za-zğüşöçıİĞÜŞÖÇ]+$",message = "Lütfen Özel Karakter, Boşluk veya Rakam Kullanmayınız")
    private String supplierDistrict;

    @NotEmpty
    @NotNull
    @Size(min=3, max=50)
    private String supplierAddress;

    @NotEmpty
    @NotNull
    @Pattern(regexp="[A-Za-z]{1}[A-Za-z0-9!#$%&'*+/=?^_`{|}~.,-]{0,20}[A-Za-z0-9]{1}[@][A-Za-z0-9]{1,20}[.][A-Za-z]{1,20}",message = "Lütfen Email Adresini Doğru Giriniz")
    private String supplierEmail;

    @NotEmpty
    @NotNull
    @Pattern(regexp="([1-9]{1}[0-9]{9})",message = "3XXXXXXXXX veya 5XXXXXXXXX Şeklinde Giriniz")
    private String phoneNumber;

    private String supplierFax;

    @NotEmpty
    @NotNull
    private String supplierTaxNo;


    @NotNull
    @Min(0)
    @Max(100)
    private Float supplierProfit;


    private Long supplierSupervisorId;

    @NotEmpty
    @NotNull
    @Size(min=2, max=20)
    @Pattern(regexp="([A-Za-zğüşöçıİĞÜŞÖÇ]{2,20})|([A-Za-zğüşöçıİĞÜŞÖÇ]{2,17}+\\s{1}+[A-Za-zğüşöçıİĞÜŞÖÇ]{2,17})",message = "Lütfen Özel Karakter Girmeyiniz veya İki İsim Arasında Birden Fazla Boşluk Bırakmayınız")
    private String name;

    @NotEmpty
    @NotNull
    @Size(min=2, max=20)
    @Pattern(regexp="[A-Za-zğüşöçıİĞÜŞÖÇ]{2,20}",message = "Lütfen Özel Karakter veya Rakam Kullanmayınız")
    private String surname;

    @NotEmpty
    @NotNull
    @Pattern(regexp="[A-Za-z0-9]{1}[A-Za-z0-9!#$%&'*+/=?^_`{|}~.,-]{0,20}[A-Za-z0-9]{1}[@][A-Za-z0-9]{1,20}[.][A-Za-z]{1,20}",message = "Lütfen Email Adresini Doğru Giriniz")
    private String email;


   // @Pattern(regexp="([A-Za-zğüşöçıİĞÜŞÖÇ]{2,20})|([A-Za-zğüşöçıİĞÜŞÖÇ]{2,17}+\\s{1}+[A-Za-zğüşöçıİĞÜŞÖÇ]{2,17})",message = "Lütfen Özel Karakter Girmeyiniz veya İki İsim Arasında Birden Fazla Boşluk Bırakmayınız")
    private String jobTitle;

    @NotEmpty
    @NotNull
    @Pattern(regexp="([5]{1}+[0-9]{9})",message = "5XXXXXXXXX Şeklinde Giriniz")
    private String supervisorPhoneNumber;

    @NotEmpty
    @NotNull
    //@Pattern(regexp="('PHARMACY'|'WAREHOUSE'|'PRODUCER')",message = "Lütfen Üretici,Depocu veya Eczane seçeneklerininden birini seçin")
    private String supplierType;


    public SupplierSaveDto() {
    }
}
