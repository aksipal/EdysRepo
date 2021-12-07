package com.via.ecza.dto;

import com.via.ecza.entity.BoxType;
import com.via.ecza.entity.DrugCard;
import lombok.Data;

import java.util.List;

@Data
public class PackagingBoxDto {

    private Long boxId;
    private String boxCode;
    private String boxNo;
    private Double boxWeight;
    private int status;

    private List<BoxDrugListDto> boxDrugList;
    private BoxType boxType;
    private BoxingDepotDto depot;
    private DrugCard drugCard;
    private BoxingSmallBoxDto smallBox;
}
