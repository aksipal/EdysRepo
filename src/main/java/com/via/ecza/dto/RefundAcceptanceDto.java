package com.via.ecza.dto;


import com.via.ecza.entity.DrugCard;
import com.via.ecza.entity.RefundStatus;
import com.via.ecza.entity.Supplier;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Data
public class RefundAcceptanceDto {


    private Long refundId;
    private Date createdAt;
    private Date expirationDate;
    private Double totalPrice;
    private Float unitPrice;
    private Long totality;
    private String refundOrderNo;
    private DrugCard drugCard;
    private String supplierName;
    private SupplierSearchDto supplier;
    private RefundStatus refundStatus;



    public RefundAcceptanceDto() { }

}
