package com.via.ecza.dto;

import lombok.Data;

import java.util.Date;

@Data
public class AccountingReportSearchDto {
    private String customerOrderNo;
    private String supplyOrderNo;
    private Date startDate;
    private Date endDate;
}
