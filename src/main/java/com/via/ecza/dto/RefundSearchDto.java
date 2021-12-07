package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Getter
@Setter
public class RefundSearchDto {
    private String refundOrderNo;
    private Long drugCard;
    private Long supplier;
    private Date createdAt;
    private Date expirationDate;
    private Double totalPrice;
    private Float unitPrice;
    private Long totality;
    private Long refundStatus;
}
