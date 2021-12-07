package com.via.ecza.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;


@Data
@Entity
@Table(name="other_receipt_price")
public class OtherReceiptPrice {

    @Id
    @SequenceGenerator(name = "sq_other_receipt_price", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_other_receipt_price")
    @Column(name = "other_receipt_price_id")
    private Long otherReceiptPriceId;

    @ManyToOne
    @JoinColumn(name="category_id",referencedColumnName = "category_id", nullable=true)
    private Category category;

    @ManyToOne
    @JoinColumn(name="drugCardId",referencedColumnName = "drug_card_id", nullable=true)
    private DrugCard drugCard;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "otherReceiptId", referencedColumnName = "other_receipt_id", nullable = true)
    private OtherReceipt otherReceipt;

    //Kâr Payı Ekip,Liva, Ex-im gibi firmalar için
    @Column(name = "profit")
    private Double profit;

    @Column(name = "vat")
    private Double vat;

    @Column(name = "vat_sum")
    private Double vatSum;

    //KDV YOK
    @Column(name = "account_total_price")
    private Double accountTotalPrice;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_supply_order_id", referencedColumnName = "customer_supply_order_id", nullable = true)
    private CustomerSupplyOrder supplyOrder;

    @Column(name = "created_at", nullable = true)
    private Date createdAt;

    @Column(name = "status")
    private int status;

}
