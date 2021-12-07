package com.via.ecza.dto;

import com.via.ecza.entity.*;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
public class PackagingDepotDto {

    private Long depotId;
    private Date expirationDate;
    private String itsNo;
    private String serialNumber;
    private String lotNo;
    private String position;
    private Date admitionDate;
    private Date sendingDate;
    private String drugBarcode;
    private String note;
    private DepotStatus depotStatus;
    private Long smallBoxId;
    private Long boxId;
    private SmallBoxDto smallBox;
    private DrugCard drugCard;
    private PackagingBoxDrugListDto boxDrugList;
    private PackagingSingleCustomerOrderDto customerOrder;

    public PackagingDepotDto(){

    }

    public PackagingDepotDto(Depot depot){

        this.depotId = depot.getDepotId();
        this.expirationDate = depot.getExpirationDate();
        this.itsNo = depot.getItsNo();
        this.serialNumber = depot.getSerialNumber();
        this.lotNo = depot.getLotNo();
        this.position = depot.getPosition();
        this.admitionDate = depot.getAdmitionDate();
        this.sendingDate = depot.getSendingDate();
        this.drugBarcode = depot.getDrugBarcode();
        this.note = depot.getNote();

        drugCard = depot.getDrugCard();
        depotStatus = depot.getDepotStatus();
    }
}
