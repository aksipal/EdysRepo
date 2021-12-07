package com.via.ecza.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class RefundAcceptanceSearchDto {

    private Long drugCardId;
    private String refundOrderNo;
    private Long supplierId;
    private Long drugCode;
    private String supplierName;

    public RefundAcceptanceSearchDto() { }

}
