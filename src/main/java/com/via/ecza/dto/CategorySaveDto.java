package com.via.ecza.dto;


import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class CategorySaveDto {

    private Long categoryId;
    @NotNull(message = "Kategori Kodu Boş Olamaz")
    @NotEmpty(message = "Kategori Kodu Boş Olamaz")
    private String code;
    private Long categoryParentId;
    private Long checkingCardId;
    @NotNull(message = "Kategori Kod Adı Boş Olamaz")
    @NotEmpty(message = "Kategori Kod Adı Boş Olamaz")
    private String name;
}
