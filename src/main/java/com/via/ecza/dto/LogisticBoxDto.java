package com.via.ecza.dto;

import lombok.Data;

@Data
public class LogisticBoxDto {

    private Long boxId;
    private String boxNo;
    private Integer drugCount;
    private String customerBoxNo;
    private Double exactBoxWeight;

}
