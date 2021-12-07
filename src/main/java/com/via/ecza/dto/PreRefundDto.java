package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Data
public class PreRefundDto {

    private Long preRefundId;
    private String drugName;
    private Long drugBarcode;
    private String drugSerialNo;
    private Date drugExpirationDate;
    private String drugLotNo;
    private String drugItsNo;
    private RefundAcceptanceDto refund;
    private Date admitionDate;
    private com.via.ecza.entity.PreRefundStatus PreRefundStatus;

    public PreRefundDto(){

    }

}
