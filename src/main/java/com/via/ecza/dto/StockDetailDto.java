package com.via.ecza.dto;

import com.via.ecza.entity.DepotStatus;
import com.via.ecza.entity.DrugCard;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Getter
@Setter
public class StockDetailDto {

    private Long depotId;
    private Date expirationDate;
    private String itsNo;
    private String lotNo;
    private String serialNumber;
    private String position;
    private Date admitionDate;
    private Date sendingDate;
    private String drugBarcode;
    private String note;
    private DepotStatus depotStatus;
    private DrugCard drugCard;
    private StockCustomerOrderListDto customerOrder;
    private StockCustomerSupplierOrderListDto customerSupplyOrder;

    public StockDetailDto(){

    }
}
