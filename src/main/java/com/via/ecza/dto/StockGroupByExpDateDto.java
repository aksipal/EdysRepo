package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Data
public class StockGroupByExpDateDto {

    private Date expirationDate;
    private String drugBarcode;
    private DrugCardDto drugCard;
    private Integer count;

    public StockGroupByExpDateDto(){

    }

}
