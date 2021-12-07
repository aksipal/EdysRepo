package com.via.ecza.dto;

import com.via.ecza.entity.Country;
import com.via.ecza.entity.CurrencyType;
import com.via.ecza.entity.enumClass.AccountType;
import lombok.Data;

import java.util.Date;

@Data
public class AmountOfDrugsGroupByMonthsDto {

   private int monthValue;
   private Long drugCount;

}
