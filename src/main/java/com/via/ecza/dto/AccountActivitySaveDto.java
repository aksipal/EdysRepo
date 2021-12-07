package com.via.ecza.dto;

import com.via.ecza.entity.enumClass.AccountType;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class AccountActivitySaveDto {

    private Long invoiceId;
    @NotEmpty
    @NotNull
    //private String invoiceNo;
    private Long checkingCardId;
    private Long otherCheckingCardId;
    private AccountType accountType;
    private Long accountId;
    private AccountType otherAccountType;
    private Long otherAccountId;

    private String bondPayerIdentityNumber;


    private Date payDay;
    private Double currencyFee;
    private String currencyType;
    private Date  documentCreatedDate;
    private Date  dateOfIssue;


    private Double price;


}
