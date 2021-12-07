package com.via.ecza.dto;

import com.sun.istack.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;


@Getter
@Setter
@Data
public class SupplierOfferSaveDto {
    private Long supplierOfferId;
    private Long offeredQuantity;
    private Float offeredSupplierProfit;
    private Long offeredSurplusQuantity;
    private String offeredSurplus;
    private Double offeredTotalPrice;
    private Long offeredTotality;
    private Float offeredAveragePrice;
    private Long surplusQuantity;
    @NotNull
    @Min(1)
    private Long quantity;

    private Float averageUnitPrice;

//    @NotNull
//    @Min(0)
//    private Float unitPrice;
    private Double totalPrice;
    @NotNull
    @Min(0)
    @Max(100)
    private Float institutionDiscount;
    @NotNull
    @Min(0)
    @Max(100)
    private Float distributorDiscount;
    @NotNull
    @Min(0)
    @Max(100)
    private Float producerDiscount;

    private Long generalPrice;

    @Pattern(regexp="([1-9]{1}[0-9]{0,20}[+]{1}[1-9]{1}[0-9]{0,20})|([1-9]{1}[0-9]{0,20}[+]{1}[0]{1})",message = "Lütfen İstenen Formatı Sağlayın --> x+x , En Düşük 1+0 Girilebilir")
    private String surplus;
    private Long totality;
    @NotNull
    @Min(0)
    @Max(100)
    private Float supplierProfit;
    private Long drugCard;
    private Long purchaseOrderDrugs;
    private Long supervisorId;
    private Long supplier;
    private Long otherCompanyId;

    public SupplierOfferSaveDto() {
    }
}
