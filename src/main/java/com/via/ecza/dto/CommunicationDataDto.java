package com.via.ecza.dto;

import lombok.Data;

import java.math.BigInteger;
import java.util.Date;

@Data
public class CommunicationDataDto {

    private BigInteger totalQuantity;
    private String barcode;
    private Date expirationDate;

}
