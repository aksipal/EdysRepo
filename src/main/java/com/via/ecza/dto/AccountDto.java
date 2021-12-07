package com.via.ecza.dto;

import com.via.ecza.entity.*;
import com.via.ecza.entity.enumClass.AccountType;
import lombok.Data;

import java.util.Date;

@Data
public class AccountDto {

    private Long accountId;
    private CheckingCardDto checkingCard;
    private AccountType accountType;
    private String accountName;
    private String bankName;
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
    private Date createdAt;
    private Country country;
    private CategoryDto category;
    private int status;

}
