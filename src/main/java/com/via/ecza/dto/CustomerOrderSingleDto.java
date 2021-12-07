package com.via.ecza.dto;

import com.via.ecza.entity.*;
import com.via.ecza.entity.Country;
import com.via.ecza.entity.CustomerOrderStatus;
import com.via.ecza.entity.User;

import java.util.Date;

public class CustomerOrderSingleDto {

    private Long customerOrderId;

    private Date orderDate;

    private Date shipmentDate;

    private String shipmentPhone;

    private String shipmentName;

    private String shipmentAdress;

    private String  orderPostalCode;

    private Long shipmentFee;

    private String tax;

    private String orderQuantity;

    private String orderAddress;

    private String orderNote;

    private Long orderTotalFee;

    private Long chargedFee;

    private Long incompleteFee;

    private String city;

    private User user;

    private CustomerOrderStatus orderStatus;

    private Country country;

    private int status;

    private CurrencyType currencyType;

    private Double currencyFee;
}
