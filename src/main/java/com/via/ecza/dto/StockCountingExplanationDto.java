package com.via.ecza.dto;

import lombok.Data;
import java.util.Date;

@Data
public class StockCountingExplanationDto {

    private Date expirationDate;
    private String itsNo;
    private String serialNumber;
    private String lotNo;
    private String drugBarcode;
    private DrugCardDto drugCard;
    private String qrCode;
    private String stockCountingExplanation;


}
