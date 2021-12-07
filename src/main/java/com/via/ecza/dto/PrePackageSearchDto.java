package com.via.ecza.dto;

import com.via.ecza.entity.enumClass.CameraType;
import lombok.Data;

@Data
public class PrePackageSearchDto {


   private Long customerOrderId;
   private Long customerSupplyOrderId;
   private CameraType cameraType;

   public PrePackageSearchDto(){

   }

}
