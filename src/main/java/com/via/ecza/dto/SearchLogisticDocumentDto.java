package com.via.ecza.dto;

import com.via.ecza.entity.enumClass.LogisticDocumentType;
import com.via.ecza.entity.enumClass.LogisticFileType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class SearchLogisticDocumentDto {
    private Long logisticDocumentId;
    private LogisticFileType fileName;
    private LogisticDocumentType documentName;
    private CustomerOrderDto customerOrderNo;
}
