package com.via.ecza.dto;

import com.sun.istack.NotNull;
import com.via.ecza.entity.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.Date;

@Data
@Setter
@Getter
public class FinalReceiptPurchaseOrderDrugsDto {

    private Long purchaseOrderDrugsId;

    private Long totalQuantity;

    private Long chargedQuantity;

    private Long incompleteQuantity;

    private Date expirationDate;

    private Float exporterUnitPrice;

    private String purchaseOrderDrugNote;

    private String purchaseOrderDrugExportNote;

    private String purchaseOrderDrugAdminNote;

}
