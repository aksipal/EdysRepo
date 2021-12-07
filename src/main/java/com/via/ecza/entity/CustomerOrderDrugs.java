package com.via.ecza.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@Getter
@Setter
@Entity
@ToString
@Table(name = "customer_order_drugs")
public class CustomerOrderDrugs {


    // sq_customer_order_drug
    @Id
    @SequenceGenerator(name = "sq_customer_order_drug", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_customer_order_drug")
    @Column(name = "customer_order_drug_id")
    private Long customerOrderDrugId;

    @Column(name = "unit_price")
    private Double unitPrice;

    @Column(name = "unit_cost")
    private Double unitCost;

    @Column(name = "surplus_of_goods1")
    private Integer surplusOfGoods1;

    @Column(name = "surplus_of_goods2")
    private Integer surplusOfGoods2;

    @Column(name = "instution_discount")
    private Float instutionDiscount;

    @Column(name = "general_discount")
    private Float generalDiscount;

    @Column(name = "is_campaigned_drug")
    private int isCampaignedDrug;

    @Column(name = "is_deleted")
    private int isDeleted;

    @Column(name = "is_added_by_manager")
    private int isAddedByManager;

    @Column(name = "profit")
    private int profit;

    private String currency;

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

    @Column(name = "english_country_name")
    private String englishCountryName;

    @Column(name = "expiration_date")
    @NotEmpty
    @NotNull
    private Date expirationDate;

    @Column(name = "created_date")
    private Date createdDate;

    @Lob
    @Type(type = "text")
    @Column(name = "customer_order_drug_note", length = 3000)
    private String customerOrderDrugNote;

    @Column(name = "purchase_order_status")
    private int purchaseOrderStatus;

    @Column(name = "purchaseOrderDrugsId")
    private Long purchaseOrderDrugsId;

    @Lob
    @Type(type = "text")
    @Column(name = "purchase_order_drugs_admin_note")
    private String purchaseOrderDrugAdminNote;

    @Enumerated(EnumType.STRING)
    @Column(name="currency_type", nullable = true)
    @NotEmpty
    @NotNull
    private CurrencyType currencyType;

    @Column(name = "currency_fee")
    private Double currencyFee;

    //navlun tl değeri
    @Column(name = "freight_cost_tl")
    private Double freightCostTl;

    @Column(name = "exact_freight_cost")
    private Double exactFreightCost;

    //navlun dövizli değeri
    @Column(name = "freight_cost_currency")
    private Double freightCostCurrency;

    //Muhasebede müşteri fişi için girilen Anlık Döviz Kuru
    @Column(name = "instant_currency_fee")
    private Double instantCurrencyFee;

    @Column(name = "depot_sale_price_excluding_vat")
    private Double depotSalePriceExcludingVat;

    @Column(name = "drug_vat")
    private Double drugVat;

    @ManyToOne
    @JoinColumn(name = "customer_order_id", referencedColumnName = "customer_order_id", nullable = true)
    private CustomerOrder customerOrder;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "drugCardId", referencedColumnName = "drug_card_id", nullable = true)
    private DrugCard drugCard;

    @ManyToOne
    @JoinColumn(name = "customer_receipt_id", referencedColumnName = "customer_receipt_id", nullable = true)
    private CustomerReceipt customerReceipt;

    @ManyToOne
    @JoinColumn(name = "campaign_id", referencedColumnName = "campaign_id", nullable = true)
    private Campaign campaign;

}

// @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
//    @JoinColumn(name = "drugid", referencedColumnName = "drug_card_id", nullable = false)
//    private DrugCard drug;