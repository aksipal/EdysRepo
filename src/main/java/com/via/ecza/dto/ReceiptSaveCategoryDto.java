package com.via.ecza.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReceiptSaveCategoryDto {
    private List<ReceiptSaveCategoryContentDto> sendReceiptCategoriesList;

}
