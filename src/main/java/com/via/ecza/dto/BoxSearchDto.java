package com.via.ecza.dto;

import com.via.ecza.entity.CustomerOrder;
import lombok.Data;

@Data
public class BoxSearchDto {

    private String boxNo;
    private String customerOrderNo;
    private Long boxId;

}
