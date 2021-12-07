package com.via.ecza.dto;

import com.via.ecza.entity.*;
import lombok.Data;
import java.util.Date;

@Data
public class InvoiceCustomerOrderDto {

    private Long customerOrderId;
    private String customerOrderNo;
    private String paymentTerms;
    private String deliveryTerms;
    private String leadTime;
    private String additionalDetails;
    private Date orderDate;
    private CurrencyType currencyType;
    private Double currencyFee;
    private String customerOrderNote;
    private String purchaseOrderNote;
    private User user;
    private CustomerOrderStatus orderStatus;
    private int status;
    private Long orderStatusHistory;
    private int logisticStatus;
}
