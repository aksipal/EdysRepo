package com.via.ecza.dto;

import lombok.Data;

import java.util.List;

@Data
public class SingleBoxDto {

    private Long boxId;
    private String boxCode;
    private String boxNo;
    private Double boxWeight;
    private int status;
    private int drugQuantity;
    private List<PackagingBoxDrugListDto> boxDrugList;


}
