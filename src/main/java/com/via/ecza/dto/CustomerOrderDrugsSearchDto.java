package com.via.ecza.dto;

import com.via.ecza.entity.CustomerOrder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Data
@Setter
@Getter
public class CustomerOrderDrugsSearchDto {

    private Date exprationDate;
    private Long drugCardId;
    private Long orderStatusId;
    private int isCampaignedDrug;

    public CustomerOrderDrugsSearchDto(){

    }



}
