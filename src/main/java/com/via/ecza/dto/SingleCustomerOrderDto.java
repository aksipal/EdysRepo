package com.via.ecza.dto;

import com.via.ecza.entity.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import java.util.Date;
import java.util.List;

@Data
@Setter
@Getter
public class SingleCustomerOrderDto {
    private Long customerOrderId;
    private Date orderDate;
    private String customerOrderNo;
    private User user;
    private CustomerOrderStatus orderStatus;
    private SingleCustomerDto customer;
    private int status;
    private String customerOrderNote;
    private Double freightCostTl;
    private List<CustomerOrderDrugsListDto> customerOrderDrugs;
    private SingleDrugCardDto drugCardDto;
    private CurrencyType currencyType;
    private Double currencyFee;
    private String paymentTerms;
    private String deliveryTerms;
    private String leadTime;
    private String additionalDetails;
    private String purchaseOrderNote;
    private SingleCompanyDto company;
    private Double preFreighCost;


    //    private CustomerOrderBankDetailDto customerOrderBankDetail;
//    private CustomerOrderShippingAdressDto customerOrderShippingAdress;
    private CustomerOrderBankDetail customerOrderBankDetail;

    private CustomerOrderShippingAdress customerOrderShippingAdress;

    public SingleCustomerOrderDto(){

    }
}
