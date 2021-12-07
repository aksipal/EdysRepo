package com.via.ecza.dto;

import com.sun.istack.NotNull;
import com.via.ecza.entity.DrugCard;
import com.via.ecza.entity.PurchaseOrderDrugs;
import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.validation.constraints.NotEmpty;
import java.util.Date;

@Data
public class PackagingPurchaseOrderDrugsDto {

    private Long purchaseOrderDrugsId;
    private Long totalQuantity;
    private Long chargedQuantity;
    private Long incompleteQuantity;
    private Date expirationDate;
    private String purchaseOrderDrugNote;
    private DrugCard drugCard;
    private PackagingCustomerOrderDto customerOrder;

    public PackagingPurchaseOrderDrugsDto(){

    }

    public PackagingPurchaseOrderDrugsDto(PurchaseOrderDrugs purchaseOrderDrugs){
        this.purchaseOrderDrugsId = purchaseOrderDrugs.getPurchaseOrderDrugsId();
        this.totalQuantity = purchaseOrderDrugs.getTotalQuantity();
        this.chargedQuantity = purchaseOrderDrugs.getChargedQuantity();
        this.incompleteQuantity = purchaseOrderDrugs.getIncompleteQuantity();
        this.expirationDate = purchaseOrderDrugs.getExpirationDate();
        this.purchaseOrderDrugNote = purchaseOrderDrugs.getPurchaseOrderDrugNote();

        customerOrder = new PackagingCustomerOrderDto(purchaseOrderDrugs.getCustomerOrder());
        drugCard = purchaseOrderDrugs.getDrugCard();


    }
}
