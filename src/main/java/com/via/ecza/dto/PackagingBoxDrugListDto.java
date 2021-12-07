package com.via.ecza.dto;

import lombok.Data;


@Data
public class PackagingBoxDrugListDto {

    private Long boxDrugListId;
    private SmallBoxDto smallBox;
    private PackagingSingleDepotDto depot;

}
