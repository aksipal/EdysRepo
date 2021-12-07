package com.via.ecza.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class PreDepotSearchDto {

   private Long customerSupplyOrderId;

   public PreDepotSearchDto(){

   }

}
