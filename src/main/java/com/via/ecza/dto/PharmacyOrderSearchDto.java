package com.via.ecza.dto;


import com.via.ecza.entity.CustomerSupplyStatus;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Getter
@Setter
public class PharmacyOrderSearchDto {

    private Long supplierId;
    private Long customerSupplyOrderId;
    private String supplyOrderNo;
    private Long customerSupplyStatus;
    private Long drugCard;
    private Date startDate;
    private Date endDate;


}
