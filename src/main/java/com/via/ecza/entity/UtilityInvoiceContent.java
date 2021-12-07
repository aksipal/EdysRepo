package com.via.ecza.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Data
@Getter
@Setter
@Entity
@Table(name="Utility_invoice_content")
public class UtilityInvoiceContent {

    @Id
    @SequenceGenerator(name = "sq_invoice", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_invoice")
    @Column(name = "Utility_invoice_content_id")
    private Long utilityInvoiceContentId;

    // hizmet satış ortak alanlar
    @Column(name = "tag_price")
    private Double tagPrice;            // etiket fiyatı
    @Column(name = "ware_houseman_price")
    private Double wareHousemanPrice;   // depocu fiyatı
    @Column(name = "general_discount")
    private int generalDiscount;        // kurum iskonto
    @Column(name = "sell_discount")
    private int sellDiscount;           // satış iskonto
    @Column(name = "advance_discount")
    private int advanceDiscount;        // satış iskonto
    @Column(name = "net_price")
    private Double netPrice;            // net fiyat
    @Column(name = "total_price")
    private Double totalPrice;          // toplam fiyat //ödenecek tutar


    // hizmet satış ve alış ortak alanlar
    @Column(name = "product_service")
    private String productService;      // Mal Hizmet
    @Column(name = "quantity")
    private Double quantity;               // miktar
    @Column(name = "unit")
    private String unit;                // Birim
    @Column(name = "unit_price")
    private Double unitPrice;           // birim fiyatı
    @Column(name = "vat")
    private Double vat;                 // kdv
    @Column(name = "vat_sum")
    private Double vatSum;              // kdv tutarı


    // hizmet alış ortak alanlar
    @ManyToOne
    @JoinColumn(name="category_id",referencedColumnName = "category_id", nullable=true)
    private Category category;         //Ürün Kodu
    @Column(name = "discount")
    private int discount;               //İskonto
    @Column(name = "discount_sum")
    private Double discountSum;         //İskonto Tutarı
    @Column(name = "product_service_sum")
    private Double productServiceSum;   //Mal Hizmet Tutarı


    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_purpose")
    private InvoicePurpose invoicePurpose;

    @Column(name = "status")
    private int status;

    @Column(name = "created_at")
    private Date createdAt;

    @ManyToOne
    @JoinColumn(name="invoice_id",referencedColumnName = "invoice_id", nullable=true)
    private Invoice invoice;

    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "userid", referencedColumnName = "user_id", nullable = true)
    private User user;

    //fatura kime kesildi bilgisi - (Liva Ekip ex-im gibi)
    @Column(name = "other_company_id")
    private Long otherCompanyId;


}
