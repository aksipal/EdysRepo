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
@Table(name = "customer_supply_order")
public class CustomerSupplyOrder {

    @Id
    @SequenceGenerator(name = "sq_customer_supply_order", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_customer_supply_order")
    @Column(name = "customer_supply_order_id")
    private Long customerSupplyOrderId;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "total_quantity")
    private Long totalQuantity;

    @Column(name = "depot_total_quantity")
    private Long depotTotalQuantity;

    @Column(name = "depot_stock_quantity")
    private Long depotStockQuantity;

    @Column(name = "quantity")
    @NotNull
    private Long quantity;

    @Column(name = "average_unit_price")
    @NotNull
    private Float averageUnitPrice;

    @Column(name = "unit_price")
    private Float unitPrice;

    @Column(name = "total_price")
    @NotNull
    private Double totalPrice;

    @Column(name = "institution_discount")
    private Float institutionDiscount;

    @Column(name = "distributor_discount")
    private Float distributorDiscount;

    @Column(name = "vat")
    private Float vat;

    @Column(name = "general_price")
    @NotNull
    private Long generalPrice;

    @Column(name = "dispatch_no")
    private String dispatchNo;

    @Column(name = "invoice_no")
    private String invoiceNo;

    @Column(name = "surplus")
    @NotEmpty
    @NotNull
    private String surplus;

    @Column(name = "surplus_quantity")
    private Long surplusQuantity;

    @Column(name = "note")
    private String note;

    @Column(name = "totality")
    @NotNull
    private Long totality;

    @Column(name = "supplier_profit")
    @NotNull
    private Float supplierProfit;

    @Column(name = "stocks")
    private Long stocks;


    @Column(name = "producer_discount")
    private Float producerDiscount;


    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_supply_status_id", referencedColumnName = "customer_supply_status_id", nullable = false)
    private CustomerSupplyStatus customerSupplyStatus;


    @Column(name = "supervisor_id")
    @NotEmpty
    @NotNull
    private Long supervisorId;

    @Column(name = "log_cso")
    private String log_cso;

    @Column(name = "supply_order_no")
    private String supplyOrderNo;

    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "drugCardId", referencedColumnName = "drug_card_id", nullable = true)
    private DrugCard drugCard;

//    @ManyToOne
//    @JoinColumn(name="customerOrderDrugId",referencedColumnName = "customer_order_drug_id")
//    private CustomerOrderDrugs customerOrderDrugs;

    @ManyToOne
    @JoinColumn(name = "purchaseOrderDrugsId", referencedColumnName = "purchase_order_drugs_id")
    private PurchaseOrderDrugs purchaseOrderDrugs;

    @ManyToOne
    @JoinColumn(name = "supplierId", referencedColumnName = "supplier_id")
    @NotEmpty
    @NotNull
    private Supplier supplier;

    @ManyToOne
    @JoinColumn(name = "receiptId", referencedColumnName = "receipt_id", nullable = true)
    private Receipt receipt;

//    @ManyToOne
//    @JoinColumn(name = "customer_receipt_id", referencedColumnName = "customer_receipt_id", nullable = true)
//    private CustomerReceipt customerReceipt;

    @Column(name = "acceptance_date")
    private Date acceptanceDate;

    @Column(name = "final_receipt_id")
    private Long finalReceiptId;

    @OneToOne( mappedBy = "supplyOrder")
    private SupplyOrderPrice supplyOrderPrice;

    @Column(name = "status")
    private int status;

    //fatura kime kesilecek bilgisi - (Liva Ekip ex-im gibi)
    @Column(name = "other_company_id")
    private Long otherCompanyId;
}
