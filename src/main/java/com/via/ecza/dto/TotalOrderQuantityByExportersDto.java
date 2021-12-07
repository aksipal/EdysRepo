package com.via.ecza.dto;

import com.via.ecza.entity.User;
import lombok.Data;

@Data
public class TotalOrderQuantityByExportersDto {

   private User user;
   private Long orderCount;

}
