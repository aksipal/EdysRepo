package com.via.ecza.dto;

import lombok.Data;

@Data
public class SingleCustomerOrderDrugsListDto {
    private Long drugListId;
    private CustomerOrderDrugsListDto drugs;
}
