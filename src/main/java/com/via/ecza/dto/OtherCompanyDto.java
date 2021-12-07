package com.via.ecza.dto;

import lombok.Data;

import java.util.Date;

@Data
public class OtherCompanyDto {

    private Long otherCompanyId;
    private String otherCompanyName;
    private Date createdDate;
    private int status;
}
