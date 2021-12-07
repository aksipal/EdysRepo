package com.via.ecza.dto;


import com.via.ecza.entity.enumClass.BoxSize;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
public class PreFreightCostDto {
    private Long customerOrderId;
    private BoxSize boxSize;


    @NotNull
    private Double preFreighCost;



}
