package com.via.ecza.dto;

import com.via.ecza.entity.*;
import lombok.Data;

import java.util.Date;

@Data
public class SingleDepotDto {

    private Long depotId;
    private DrugCard drugCard;
    private CustomerOrder customerOrder;
    private CustomerSupplyOrder customerSupplyOrder;
    private Date expirationDate;
    private String itsNo;
    private String serialNumber;
    private String lotNo;
    private String position;
    private Date admitionDate;
    private Date sendingDate;
    private String drugBarcode;
    private String note;
    private Long smallBoxId;
    private Long boxId;
    private BoxDrugList boxDrugList;
    private DepotStatus depotStatus;
    private RefundOffer refundOffer;
    private Refund refund;

}
