package com.via.ecza.dto;

import com.via.ecza.entity.DrugCard;
import com.via.ecza.entity.Supplier;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@Getter
@Setter
public class RefundOfferSaveDto {
    @NotNull
    private Date expirationDate;
    @NotNull
    private Double totalPrice;
    @NotNull
    private Float unitPrice;
    @NotNull
    private Long totality;
    private Long offeredTotality;
    private Double offeredTotalPrice;
    @NotNull
    private Long drugCard;
    @NotNull
    private Long supplier;
}
