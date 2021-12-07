package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ReceiptCheckListDto {
    private Long customerSupplyOrderId;
    private Boolean value;
}
