package com.via.ecza.dto;

import lombok.Data;

import java.util.Date;

@Data
public class AccountingCodeDto {
    private Long accountingCodeId;
    private String code;
    private String name;
    private Boolean reverseWorkingAccount;
    private Date createdDate;
    private int status;
    private AccountingCodeCategoryDto category;

    public AccountingCodeDto() {

    }
}
