package com.via.ecza.dto;

import lombok.Data;

import java.util.Date;

@Data
public class ParentCategoryDto {
    private Long categoryId;


    private String name;

    private Integer code;

    private Date createdDate;

    private int status;
}
