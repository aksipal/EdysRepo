package com.via.ecza.dto;

import com.via.ecza.entity.enumClass.AccountType;
import com.via.ecza.entity.CurrencyType;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class AccountSaveDto {

    private Long checkingCardId;
    private Long accountId;
    private AccountType accountType;

    @NotNull
    @NotEmpty
    private String accountName;
    private String bankName;
    private Long countryId;
    private String ibanNo;
    private String swiftNo;
    private CurrencyType swiftType;
    private String accountNo;
    private String branchNo;
    private String branchCity;
    private String branchDistrict;
    private String branchName;
    private String bankSupervisor;
    private Date bondPayDay;
    private Double bondPayableAmount;
    private String bondGiver;
    private String bondPayer;
    private String bondPaymentType;
    private String bondPayerAdress;
    private String bondPayerTaxOffice;
    private String bondPayerTaxNo;
    private String bondPayerIdentityNumber;
    private Date bondDateOfIssue;
    private Date chequeDate;
    private String chequePayableAmount;
    private String chequeOwner;
    private Date chequePrintingDate;
    private String chequeAccountOwner;
    private String note;
    private Long categoryId;
    private int status;
}
