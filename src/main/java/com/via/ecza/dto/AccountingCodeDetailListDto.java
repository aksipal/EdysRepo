package com.via.ecza.dto;

import com.via.ecza.entity.AccountingCode;
import com.via.ecza.entity.CurrencyType;
import lombok.Data;

import java.util.Date;

@Data
public class AccountingCodeDetailListDto {

    private Long accountActivityId;

    //Cari Hesabı
    private AccountDto account;

    //Karşı Cari Hesabı
    private AccountDto otherAccount;

    private CheckingCardDto checkingCard;

    private CheckingCardDto otherCheckingCard;

    private InvoiceDto invoice;

    private CurrencyType currencyType;

    private Double currencyFee;

    private Date createdAt;

    //Alacak
    private Double charge;

    //Borç
    private Double debt;

    //Ödeme Tarihi - Vade
    private Date dateOfIssue;

    //Tahsil Edildiği Tarih
    private Date paidDay;

    //Belge Oluşturulma Tarihi
    private Date documentCreatedDate;

    private String bondPayerIdentityNumber;

    private AccountingCode accountingCode;

}
