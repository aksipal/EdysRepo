package com.via.ecza.dto;

import com.via.ecza.entity.DepotStatus;
import com.via.ecza.entity.DrugCard;
import com.via.ecza.entity.User;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Data
public class StockDepotDto {
    
    private Long depotId;
    private Date expirationDate;
    private String itsNo;
    private String serialNumber;
    private String lotNo;
    private String position;
    private Date admitionDate;
    private Date sendingDate;
    private String customerOrderNo;
    private String drugBarcode;
    private String note;
    private DepotStatus depotStatus;
    private DrugCard drugCard;
    private User user;
    private StockCustomerOrderDto customerOrder;
    private StockCustomerSupplierOrderListDto customerSupplyOrder;


    public StockDepotDto(){

    }
}
