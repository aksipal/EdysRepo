package com.via.ecza.dto;

import com.via.ecza.entity.enumClass.LogisticDocumentType;
import com.via.ecza.entity.enumClass.LogisticFileType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CustomerOrderLogisticDocumentListDto {
    private Long customerOrderLogisticDocumentId;
    private LogisticFileType fileType;
    private LogisticDocumentType documentType;
    private CustomerOrderDto customerOrder;
    private String fileName;

}
