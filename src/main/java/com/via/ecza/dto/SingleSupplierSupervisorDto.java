package com.via.ecza.dto;


import com.via.ecza.entity.SupplierSupervisor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Pattern;

@Data
@Getter
@Setter
public class SingleSupplierSupervisorDto {

    private Long supplierSupervisorId;
    private Long supplierId;
    @Pattern(regexp="([A-Za-zğüşöçıİĞÜŞÖÇ]{2,20})|([A-Za-zğüşöçıİĞÜŞÖÇ]{2,17}+\\s{1}+[A-Za-zğüşöçıİĞÜŞÖÇ]{2,17})",message = "Lütfen Özel Karakter Girmeyiniz veya İki İsim Arasında Birden Fazla Boşluk Bırakmayınız")
    private String name;
    @Pattern(regexp="[A-Za-zğüşöçıİĞÜŞÖÇ]{2,20}",message = "Lütfen Özel Karakter veya Rakam Kullanmayın")
    private String surname;
    @Pattern(regexp="[A-Za-z0-9]{1}[A-Za-z0-9!#$%&'*+/=?^_`{|}~.,-]{0,20}[A-Za-z0-9]{1}[@][A-Za-z0-9]{1,20}[.][A-Za-z]{1,20}",message = "Lütfen Email Adresini Doğru Girin")
    private String email;
    @Pattern(regexp="([5]{1}+[0-9]{9})",message = "5XXXXXXXXX Şeklinde Giriniz")
    private String phoneNumber;
    private String jobTitle;
    private int status;

    public SingleSupplierSupervisorDto(SupplierSupervisor supplierSupervisor) {
        this.supplierSupervisorId = supplierSupervisor.getSupplierSupervisorId();
        this.name = supplierSupervisor.getName();
        this.surname = supplierSupervisor.getSurname();
        this.email = supplierSupervisor.getEmail();
        this.phoneNumber = supplierSupervisor.getPhoneNumber();
        this.jobTitle = supplierSupervisor.getJobTitle();
        this.supplierId = supplierSupervisor.getSupplier().getSupplierId();
        this.status = supplierSupervisor.getSupplier().getStatus();
    }

    public SingleSupplierSupervisorDto() {
    }

}
