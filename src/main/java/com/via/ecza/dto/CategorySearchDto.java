package com.via.ecza.dto;


import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class CategorySearchDto {

    private Long categoryId;
    private String code;
    private Long categoryParentId;
    private String codeValue;
    @NotNull
    @NotEmpty
    private String name;
}
