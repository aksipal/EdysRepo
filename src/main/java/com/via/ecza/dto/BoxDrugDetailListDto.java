package com.via.ecza.dto;

import lombok.Data;


@Data
public class BoxDrugDetailListDto {

    private Long boxDrugListId;
    private SingleSmallBoxDto smallBox;
    private PackagingSingleDepotDto depot;

}
