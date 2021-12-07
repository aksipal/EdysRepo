package com.via.ecza.dto;

import lombok.Data;

import java.math.BigInteger;
import java.util.Date;

@Data
public class SmallBoxListDetailDto {

    private BigInteger count;
    private String drugName;
    private BigInteger drugCode;
    private Date expirationDate;
}
