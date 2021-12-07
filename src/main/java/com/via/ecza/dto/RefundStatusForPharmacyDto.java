package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class RefundStatusForPharmacyDto {

    private Long refundStatusId;

    private String statusName;
    private String explanation;
}
