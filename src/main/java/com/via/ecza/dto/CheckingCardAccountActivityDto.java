package com.via.ecza.dto;

import com.via.ecza.entity.*;
import com.via.ecza.entity.enumClass.AccountActivityType;
import lombok.Data;

import java.util.Date;

@Data
public class CheckingCardAccountActivityDto {

    private Long accountActivityId;
    private CheckingCardDto checkingCard;
    private CheckingCardDto otherCheckingCard;
    private InvoiceDto invoice;
    private String invoiceNo;
    private CurrencyType currencyType;
    private AccountActivityType accountActivityType;
    private Double currencyFee;
    private Date createdAt;
    private Double charge;
    private Double debt;
    private Date dateOfIssue;
    private Date paidDay;
    private Date documentCreatedDate;
    private String bondPayerIdentityNumber;
}
