package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class RefundCheckListDto {
    private Long refundId;
    private Long receiptId;
    private Boolean value;
}
