package com.via.ecza.dto;

import com.via.ecza.entity.PreDepotStatus;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Lob;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Data
public class PreDepotDto {

    private Long preDepotId;
    @Lob
    private String drugName;
    private Long drugBarcode;
    private String drugSerialNo;
    private Date drugExpirationDate;
    private String drugLotNo;
    private String drugItsNo;
    private DepotCustomerOrderListDto customerOrder;
    private DepotCustomerSupplierOrderListDto customerSupplyOrder;
    private Date admitionDate;
    private PreDepotStatus preDepotStatus;
    private List<StockCountingDrugSummary> drugSummaryList;

    public PreDepotDto(){

    }

}
