package com.via.ecza.dto;


import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Lob;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
public class ReceiptCreateDto {

    @Lob
    private String invoiceNote;
    @NotNull
    private String invoiceNo;

    @NotNull(message = " Boş Değer Olamaz")
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date invoiceDate;
    @Lob
    private String receiptNote;
    private List<ReceiptCheckListDto> checkedList;
}
