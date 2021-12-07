package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class OrderAcceptanceDto {
    private String customerOrderNo;
    private String customerSupplyOrderNo;


    public OrderAcceptanceDto(){

    }

}
