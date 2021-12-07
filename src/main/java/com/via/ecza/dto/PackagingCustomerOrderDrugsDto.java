package com.via.ecza.dto;

import com.sun.istack.NotNull;
import com.via.ecza.entity.CustomerOrderDrugs;
import com.via.ecza.entity.DrugCard;
import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.validation.constraints.NotEmpty;
import java.util.Date;

@Data
public class PackagingCustomerOrderDrugsDto {
    private Long customerOrderId;
    private Long customerOrderDrugId;
    private Double unitPrice;
    private Double unitCost;
    private Integer surplusOfGoods1;
    private Integer surplusOfGoods2;
    private String currency;
    private Long totalQuantity;
    private Long chargedQuantity;
    private Long incompleteQuantity;
    private Date expirationDate;
    private String customerOrderDrugNote;
    private Long depotCountWithSmallBox;
    private DrugCard drugCard;

    private PackagingCustomerOrderDto customerOrder;

    public PackagingCustomerOrderDrugsDto() {

    }
    public PackagingCustomerOrderDrugsDto(CustomerOrderDrugs drugs) {

        this.customerOrderDrugId = drugs.getCustomerOrderDrugId();
        this.unitPrice = drugs.getUnitPrice();
        this.unitCost = drugs.getUnitCost();
        this.surplusOfGoods1 = drugs.getSurplusOfGoods1();
        this.surplusOfGoods2 = drugs.getSurplusOfGoods2();
        this.currency = drugs.getCurrency();
        this.totalQuantity = drugs.getTotalQuantity();
        this.chargedQuantity = drugs.getChargedQuantity();
        this.incompleteQuantity = drugs.getIncompleteQuantity();
        this.expirationDate = drugs.getExpirationDate();
        this.customerOrderDrugNote = drugs.getCustomerOrderDrugNote();

        drugCard = drugs.getDrugCard();
        customerOrder = new PackagingCustomerOrderDto(drugs.getCustomerOrder());
    }
}
