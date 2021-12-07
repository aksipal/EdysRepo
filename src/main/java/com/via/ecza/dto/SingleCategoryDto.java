package com.via.ecza.dto;


import lombok.Data;

import java.util.Date;

@Data
public class SingleCategoryDto {


    private Long categoryId;

    private ParentCategoryDto parentCategory;

    private String name;

    private String code;

    private Date createdDate;

    private int status;

}
