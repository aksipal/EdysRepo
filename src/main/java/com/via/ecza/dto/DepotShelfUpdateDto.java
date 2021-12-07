package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Setter
@Getter
public class DepotShelfUpdateDto {
    private String drugPosition;
    private List<DepotShelfCheckedListDto> checkedList;


    public DepotShelfUpdateDto(){

    }

}
