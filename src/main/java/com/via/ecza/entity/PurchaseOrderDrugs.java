package com.via.ecza.entity;


import com.sun.istack.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.Date;

@Data
@Getter
@Setter
@Entity
@Table(name = "purchase_order_drugs")
public class PurchaseOrderDrugs {


    @Id
    @SequenceGenerator(name = "sq_purchase_order_drugs", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_purchase_order_drugs")
    @Column(name = "purchase_order_drugs_id")
    private Long purchaseOrderDrugsId;


    @Column(name = "total_quantity")
    @NotEmpty
    @NotNull
    private Long totalQuantity;

    @Column(name = "charged_quantity")
    @NotEmpty
    @NotNull
    private Long chargedQuantity;

    @Column(name = "incomplete_quantity")
    @NotEmpty
    @NotNull
    private Long incompleteQuantity;


    @Column(name = "expiration_date")
    @NotEmpty
    @NotNull
    private Date expirationDate;

    @Column(name = "exporter_unit_price")
    private Float exporterUnitPrice;

    @Lob
    @Type(type = "text")
    @Column(name = "purchase_order_drugs_note", length = 3000)
    private String purchaseOrderDrugNote;

    @Lob
    @Type(type = "text")
    @Column(name = "purchase_order_drugs_export_note", length = 3000)
    private String purchaseOrderDrugExportNote;

    @Lob
    @Type(type = "text")
    @Column(name = "purchase_order_drugs_admin_note", length = 3000)
    private String purchaseOrderDrugAdminNote;

    @ManyToOne
    @JoinColumn(name = "customer_order_id", referencedColumnName = "customer_order_id", nullable = false)
    private CustomerOrder customerOrder;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "drugCardId", referencedColumnName = "drug_card_id", nullable = true)
    private DrugCard drugCard;

    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "purchase_status_id", referencedColumnName = "purchase_status_id", nullable = false)
    private PurchaseStatus purchaseStatus;


}
