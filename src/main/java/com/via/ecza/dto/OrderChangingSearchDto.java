package com.via.ecza.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class OrderChangingSearchDto {

    private Long drugCardId;
    private String supplyOrderNo;
    private Long supplierId;
    private Long drugCode;
    private String supplierName;



    public OrderChangingSearchDto() { }

}
