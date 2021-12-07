package com.via.ecza.dto;

import lombok.Data;

@Data
public class CustomerOrderBankDetailDto {

    private Long customerOrderBankDetailId;
    private String accountName;
    private String bankName;
    private String ibanNo;
    private String swift;
    private CustomerOrderDto customerOrder;
}
