package com.via.ecza.entity;

import com.sun.istack.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.Date;


@Data
@Getter
@Setter
@Entity
@Table(name = "supplier_offer")
public class SupplierOffer {


    @Id
    @SequenceGenerator(name = "sq_supplier_offer", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_supplier_offer")
    @Column(name = "supplier_offer_id")
    private Long supplierOfferId;

    @Column(name = "quantity")
    @NotNull
    private Long quantity;

    @Column(name = "offered_quantity")
    private Long offeredQuantity;

    @Column(name = "average_unit_price")
    private Float averageUnitPrice;

    @Column(name = "unit_price")
    private Float unitPrice;

    @Column(name = "total_price")
    private Double totalPrice;

    @Column(name = "institution_discount")
    private Float institutionDiscount;

    @Column(name = "distributor_discount")
    private Float distributorDiscount;

    @Column(name = "vat")
    private Float vat;

    @Column(name = "general_price")
    private Long generalPrice;

    @Column(name = "surplus")
    @NotEmpty
    @NotNull
    private String surplus;

    @Column(name = "surplus_quantity")
    private Long surplusQuantity;

    @Column(name = "offered_surplus")
    private String offeredSurplus;

    @Column(name = "offered_surplus_quantity")
    private Long offeredSurplusQuantity;

    @Column(name = "offered_totality")
    private Long offeredTotality;

    @Column(name = "offered_total_price")
    private Double offeredTotalPrice;

    @Column(name = "offered_average_price")
    private Float offeredAveragePrice;

    @Column(name = "stocks")
    private Long stocks;

    @Column(name = "note")
    private String note;

    @Column(name = "log_so")
    private String log_so;

    @Column(name = "totality")
    @NotNull
    private Long totality;

    @Column(name = "total_quantity")
    private Long totalQuantity;

    @Column(name = "supplier_profit")
    @NotNull
    private Float supplierProfit;

    @Column(name = "offered_supplier_profit")
    private Float offeredSupplierProfit;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "producer_discount")
    private Float producerDiscount;

    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "supplier_offer_status_id", referencedColumnName = "supplier_offer_status_id", nullable = false)
    private SupplierOfferStatus supplierOfferStatus;


    @Column(name = "supervisor_id")
    @NotNull
    private Long supervisorId;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "drugCardId", referencedColumnName = "drug_card_id", nullable = true)
    private DrugCard drugCard;


    @ManyToOne
    @JoinColumn(name = "purchaseOrderDrugsId", referencedColumnName = "purchase_order_drugs_id")
    private PurchaseOrderDrugs purchaseOrderDrugs;

    @ManyToOne
    @JoinColumn(name = "supplierId", referencedColumnName = "supplier_id")
    @NotEmpty
    @NotNull
    private Supplier supplier;

    //fatura kime kesilecek bilgisi
    @Column(name = "other_company_id")
    private Long otherCompanyId;

}
