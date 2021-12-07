package com.via.ecza.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoxDrugNameDto {

    private String barcode;
    private String drugName;
    private String quantity;
    private Date expirationDate;
    private int status;
    private Long drugCardId;

}
