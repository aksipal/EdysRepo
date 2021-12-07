package com.via.ecza.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.List;


@Data
@Getter
@Setter
@Entity
@Table(name="invoice")
public class Invoice {

    @Id
    @SequenceGenerator(name = "sq_invoice", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_invoice")
    @Column(name = "invoice_id")
    private Long invoiceId;

    @Column(name = "invoice_no")
    private String invoiceNo;

    @OneToOne
    @JoinColumn(name="user_id",referencedColumnName = "user_id", nullable=true)
    private User user;

//    @OneToOne(mappedBy = "invoice" )
//    private FinalReceipt finalReceipt;

    @OneToOne(mappedBy = "invoice" )
    private Receipt receipt;

    @OneToOne(mappedBy = "invoice" )
    private CustomerReceipt customerReceipt;

    @OneToOne(mappedBy = "invoice" )
    private RefundReceipt refundReceipt;

    @OneToOne(mappedBy = "invoice" )
    private OtherRefundReceipt otherRefundReceipt;

    @OneToOne(mappedBy = "invoice" )
    private OtherReceipt otherReceipt;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "invoice_date")
    private Date invoiceDate;

    @Column(name = "tax_no")
    private String taxNo;

    @Column(name = "tax_office")
    private String taxOffice;

    //mersis
    @Column(name = "crs_no")
    private String crsNo;

    @Column(name = "status")
    private int status;

    @Column(name = "due_date")
    private Date dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name="currency_type")
    private CurrencyType currencyType;

    @Column(name = "currency_fee")
    private Double currencyFee;

    //toplam tutar (tl)
    @Column(name = "total_price")
    private Double totalPrice;

    //toplam tutar (tl) (liva)
    @Column(name = "total_price_liva")
    private Double totalPriceLiva;

    //ödeme eklendikçe artan değişken
    @Column(name = "total_charge_price")
    private Double totalChargePrice;

    //toplam tutar
    @Column(name = "total_price_currency")
    private Double totalPriceCurrency;

    //toplam tutar (liva)
    @Column(name = "total_price_currency_liva")
    private Double totalPriceCurrencyLiva;

    //navlun (tl)
    @Column(name = "freight_cost_tl")
    private Double freightCostTl;

    //navlun
    @Column(name = "freight_cost_currency")
    private Double freightCostCurrency;

    //Ödenecek Tutar (TL) Açılımı
    @Column(name = "total_price_expression")
    private String totalPriceExpression;

    //Ödenecek Tutar Açılımı
    @Column(name = "total_price_currency_expression")
    private String totalPriceCurrencyExpression;

    //Fatura Vade Tarihi
    @Column(name = "payment_term")
    private Date paymentTerm;

    //Anlık Döviz Kuru
    @Column(name = "instant_currency_fee")
    private Double instantCurrencyFee;

    //Liva üzerinden alım varsa livanın aldığı tutar bu alana set edilecek
    @Column(name = "charge_liva")
    private Double chargeLiva;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "invoice_status_id", referencedColumnName = "invoice_status_id", nullable = true)
    private InvoiceStatus invoiceStatus;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "invoice_type_id", referencedColumnName = "invoice_type_id", nullable = true)
    private InvoiceType invoiceType;

    //Fatura Eden
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "checking_card_id", referencedColumnName = "checking_card_id", nullable = true)
    private CheckingCard checkingCard;

    //Fatura Edilen
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "other_checking_card_id", referencedColumnName = "checking_card_id", nullable = true)
    private CheckingCard otherCheckingCard;

    //Fatura Eden Kategori
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", referencedColumnName = "category_id", nullable = true)
    private Category category;

    //Fatura Edilen Kategori
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "other_category_id", referencedColumnName = "category_id", nullable = true)
    private Category otherCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_purpose")
    private InvoicePurpose invoicePurpose;

    @OneToMany(mappedBy = "invoice")
    private List<UtilityInvoiceContent> utilityInvoiceContents;

    @OneToMany(mappedBy = "invoice")
    private List<AccountActivity> accountActivities;

    //fatura kime kesildi bilgisi - (Liva Ekip ex-im gibi)
    @Column(name = "other_company_id")
    private Long otherCompanyId;

//
//    // hizmet satış ortak alanlar
//    @Column(name = "tag_price")
//    private Double tagPrice;            // etiket fiyatı
//    @Column(name = "ware_houseman_price")
//    private Double wareHousemanPrice;   // depocu fiyatı
//    @Column(name = "general_discount")
//    private int generalDiscount;        // kurum iskonto
//    @Column(name = "sell_discount")
//    private int sellDiscount;           // satış iskonto
//    @Column(name = "advance_discount")
//    private int advanceDiscount;        // satış iskonto
//    @Column(name = "net_price")
//    private Double netPrice;            // net fiyat
//    @Column(name = "total_price")
//    private Double totalPrice;          // toplam fiyat //ödenecek tutar
//
//
//    // hizmet satış ve alış ortak alanlar
//    @Column(name = "product_service")
//    private String productService;      // Mal Hizmet
//    @Column(name = "quantity")
//    private int quantity;               // miktar
//    @Column(name = "unit")
//    private String unit;                // Birim
//    @Column(name = "unit_price")
//    private Double unitPrice;           // birim fiyatı
//    @Column(name = "vat")
//    private Double vat;                 // kdv
//    @Column(name = "vat_sum")
//    private Double vatSum;              // kdv tutarı
//
//
//
//    // hizmet alış ortak alanlar
//    @Column(name = "product_code")
//    private String productCode;         //Ürün Kodu
//    @Column(name = "discount")
//    private int discount;               //İskonto
//    @Column(name = "discount_sum")
//    private Double discountSum;         //İskonto Tutarı
//    @Column(name = "product_service_sum")
//    private Double productServiceSum;   //Mal Hizmet Tutarı

}
