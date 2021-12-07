package com.via.ecza.dto;

import lombok.Data;
import java.util.List;

@Data
public class FinalReceiptCreateDto {

    private List<FinalReceiptCheckListDto> checkList;
    private Long supplierId;
    private Long finalReceiptId;

}
