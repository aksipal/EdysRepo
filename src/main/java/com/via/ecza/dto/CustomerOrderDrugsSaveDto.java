package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.*;
import java.util.Date;

@Data
@Setter
@Getter
public class CustomerOrderDrugsSaveDto {

    @NotNull
    private Long drugCardId;
    private Long customerOrderId;
    private Long customerId;
    private Double freightCostTl;
    @NotNull
    @Future(message = "Gelecek bir tarih belirleyiniz.")
    private Date expirationDate;

    @NotNull
    @Positive(message = "Kurum İskonto Boş ve Negatif Olamaz.")
    private Long totalQuantity;
    private Long unitPrice;
    private String customerOrderDrugNote;
    private Date createdDate;
    private Double unitCost;
    private Integer surplusOfGoods1;
    private Integer surplusOfGoods2;
    private int isCampaignedDrug;



    public CustomerOrderDrugsSaveDto(){


    }


}
