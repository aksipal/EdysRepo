package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Setter
@Getter
public class OrderAcceptanceAcceptDto {
    private String drugPosition;
    private List<AcceptanceCheckedListDto> checkedList;
    private String stockCountingExplanation;


    public OrderAcceptanceAcceptDto(){

    }

}
