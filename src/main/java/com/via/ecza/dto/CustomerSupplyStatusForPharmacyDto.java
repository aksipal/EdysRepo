package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CustomerSupplyStatusForPharmacyDto {

    private Long customerSupplyStatusId;

    private String statusName;
    private String explanation;

}
