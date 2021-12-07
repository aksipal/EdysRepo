package com.via.ecza.dto;

import com.via.ecza.entity.*;
import lombok.Data;

import java.util.Date;

@Data
public class FinalReceiptComboboxDto {
    private Long finalReceiptId;
    private String finalReceiptNo;
    private int status;
    private Date createdAt;
    private FinalReceiptStatus finalReceiptStatus;
    private ReceiptType finalReceiptType;
}
