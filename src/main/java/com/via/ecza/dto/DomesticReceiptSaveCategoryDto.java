package com.via.ecza.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class DomesticReceiptSaveCategoryDto {
    private Date invoiceDate;
    private String invoiceNo;
    private List<DomesticReceiptSaveCategoryContentDto> sendReceiptCategoriesList;

}
