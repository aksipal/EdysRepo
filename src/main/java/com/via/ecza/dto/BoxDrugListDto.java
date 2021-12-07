package com.via.ecza.dto;

import lombok.Data;


@Data
public class BoxDrugListDto {

    private Long boxDrugListId;
    private DepotDto depot;
    private SmallBoxDto smallBox;

}
