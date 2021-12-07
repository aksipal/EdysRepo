package com.via.ecza.dto;

import com.via.ecza.entity.enumClass.BoxSize;
import lombok.Data;
import java.util.Date;

@Data
public class PreLogisticDto {

    private Long preLogisticCalculationId;
    private BoxSize boxSize;
    private Long restOfBoxVolume;
    private Long totalBoxVolume;
    private Long totalDrugCount;
    private Double totalBoxWeight;
    private Date createdAt;
    private int status;
}
