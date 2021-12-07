package com.via.ecza.dto;

import com.via.ecza.entity.CustomerOrderStatus;
import lombok.Data;
import java.util.Date;

@Data
public class CustomerOrderStatusHistoryDto {

    private Long orderStatusHistoryId;
    private String statusName;
    private CustomerOrderStatus customerOrderStatus;
    private Date createdDate;
}
