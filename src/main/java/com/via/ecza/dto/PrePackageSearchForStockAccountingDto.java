package com.via.ecza.dto;

import com.via.ecza.entity.enumClass.CameraType;
import lombok.Data;

@Data
public class PrePackageSearchForStockAccountingDto {

//    private Long customerOrderId;
//    private String itsNo;
    private CameraType cameraType;

    public PrePackageSearchForStockAccountingDto(){

    }
}
