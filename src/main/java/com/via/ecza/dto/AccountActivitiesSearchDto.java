package com.via.ecza.dto;

import lombok.Data;

import java.util.Date;

@Data
public class AccountActivitiesSearchDto {
    private String invoiceNo;
    private Date startDate;
    private Date endDate;
}
