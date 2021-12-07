package com.via.ecza.dto;

import com.via.ecza.entity.CurrencyType;
import com.via.ecza.entity.CustomerOrder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class PackagingCustomerOrderDto {

    private Long customerOrderId;
    private String customerOrderNo;
    private Date orderDate;
    private CurrencyType currencyType;
    private Double currencyFee;
    private String customerOrderNote;
    private List<PackagingCustomerOrderDrugsListDto> customerOrderDrugs;
    private SingleCustomerDto customer;

    public PackagingCustomerOrderDto() {

    }
    public PackagingCustomerOrderDto(CustomerOrder order) {

        this.customerOrderId = order.getCustomerOrderId();
        this.customerOrderNo = order.getCustomerOrderNo();
        this.orderDate = order.getOrderDate();
        this.currencyType = order.getCurrencyType();
        this.currencyFee = order.getCurrencyFee();
        this.customerOrderNote = order.getCustomerOrderNote();
    }
}

