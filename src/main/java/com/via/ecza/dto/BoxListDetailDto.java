package com.via.ecza.dto;

import lombok.Data;

import java.math.BigInteger;
import java.util.Date;

@Data
public class BoxListDetailDto {
    private BigInteger count;
    private String drugName;
    private BigInteger drugCode;
    private String smallBoxNo;
    private Date expirationDate;
}
