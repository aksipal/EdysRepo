package com.via.ecza.dto;

import lombok.Data;

import java.util.Date;

@Data
public class CheckinCardActivitySearchDto {

    private Long checkingCardId;
    private Date startDate;
    private Date endDate;
    private Long otherCheckingCardId;
    private String invoiceNo;
}
