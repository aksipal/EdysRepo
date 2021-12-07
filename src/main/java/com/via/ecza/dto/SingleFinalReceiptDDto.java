package com.via.ecza.dto;

import com.via.ecza.entity.ReceiptType;
import lombok.Data;

import java.util.Date;

@Data
public class SingleFinalReceiptDDto {

    private Long finalReceiptId;
    private String finalReceiptNo;
}
