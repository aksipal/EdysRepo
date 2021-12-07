package com.via.ecza.dto;

import com.via.ecza.entity.*;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
public class SingleCustomerOrderInvoiceDto {

    private Long invoiceId;
    private String invoiceNo;
    private User user;
    private Date createdAt;
    private Date invoiceDate;
    private String taxNo;
    private String taxOffice;
    private String crsNo;
    private int status;
    private Date dueDate;
    private CurrencyType currencyType;
    private Double currencyFee;
    private Double totalPrice;
    private Double totalPriceCurrency;
    private Double freightCostTl;
    private Double freightCostCurrency;
    private String totalPriceExpression;
    private String totalPriceCurrencyExpression;
    private Date paymentTerm;
    private Double instantCurrencyFee;
    private InvoiceStatus invoiceStatus;
    private InvoiceType invoiceType;

}
