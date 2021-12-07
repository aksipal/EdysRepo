package com.via.ecza.dto;

import com.sun.istack.NotNull;
import lombok.Data;

import javax.persistence.Column;
import javax.validation.constraints.NotEmpty;

@Data
public class CustomerOrderTotalPackagingDataDto {

    private Long customerOrderId;
    private Long chargedQuantity;
    private Long incompleteQuantity;
    private Long totalQuantity;
}
