package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class DepotGroupByLotNoDto {

    private String lotNo;
    private String drugBarcode;
    private DrugCardDto drugCard;
    private Integer count;

    public DepotGroupByLotNoDto(){

    }

}
