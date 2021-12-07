package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Setter
@Getter
public class RefundAcceptanceManualDto {
    private Long refundId;
    private Date stt;
    private Integer quantity;
    private String lotNo;


    public RefundAcceptanceManualDto(){

    }

}
