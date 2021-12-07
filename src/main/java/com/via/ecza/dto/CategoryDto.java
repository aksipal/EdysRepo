package com.via.ecza.dto;

import com.via.ecza.entity.Category;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class CategoryDto {

    private Long categoryId;
    private Category parentCategory;
    private String name;
    private String code;
    private String codeValue;
    private Date createdDate;
    private int status;
    private int vatValue;

    private List<AccountingCodeDto> accountingCodes;
}
