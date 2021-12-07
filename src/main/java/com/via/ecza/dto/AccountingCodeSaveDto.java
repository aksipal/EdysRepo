package com.via.ecza.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class AccountingCodeSaveDto {
    @NotNull(message="Kod Boş Olamaz.")
    @NotEmpty(message="Kod Boş Olamaz")
    private String code;
    @NotNull(message="Ad Boş Olamaz")
    @NotEmpty(message="Ad Boş Olamaz")
    private String name;
    private Boolean reverseWorkingAccount;
    private int status;
    private int categoryId;

    public AccountingCodeSaveDto() {

    }
}
