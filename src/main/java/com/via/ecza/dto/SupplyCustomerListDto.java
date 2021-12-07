package com.via.ecza.dto;

import com.via.ecza.entity.PurchaseStatus;
import com.via.ecza.entity.User;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;


@Data
@Setter
@Getter
public class SupplyCustomerListDto {
    private Long customerOrderId;
    private String customerOrderNo;
    private Date orderDate;
    private int status;
    private PurchaseStatus purchaseStatus;
    private User user;
    private SingleCustomerDto customer;

    public SupplyCustomerListDto() {
    }
}
