package com.via.ecza.dto;

import com.sun.istack.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.validation.constraints.NotEmpty;
import java.util.Date;

@Data
@Getter
@Setter
public class CustomerOrderDrugUpdateDto {

    private Long customerOrderDrugId;

    private Long customerOrderId;

    private Long drugCardId;

    private Double unitPrice;

    private Double unitCost;

    private Integer surplusOfGoods1;

    private Integer surplusOfGoods2;
    private int isCampaignedDrug;

    private Double freightCostTl;


    //    @NotEmpty
    //    @NotNull
    private Long totalQuantity;

    //    @NotEmpty
    //    @NotNull
    private Date expirationDate;

    @Lob
    private String customerOrderDrugNote;
}
