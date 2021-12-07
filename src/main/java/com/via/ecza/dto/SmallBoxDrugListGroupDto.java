package com.via.ecza.dto;

import lombok.Data;

import java.math.BigInteger;
import java.util.Date;

@Data
public class SmallBoxDrugListGroupDto {

    private String drugName;
    private String lotNo;
    private BigInteger count;
    private Date expirationDate;

}
