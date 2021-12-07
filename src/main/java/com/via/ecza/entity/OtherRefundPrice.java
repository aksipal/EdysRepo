package com.via.ecza.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "other_refund_price")
public class OtherRefundPrice {

    @Id
    @SequenceGenerator(name = "sq_other_refund_price", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_other_refund_price")
    @Column(name = "other_refund_price_id")
    private Long otherRefundPriceId;

    @Column(name = "vat")
    private Double vat;

    @Column(name = "vat_sum")
    private Double vatSum;

    @Column(name = "account_total_price")
    private Double accountTotalPrice;


    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "refund_id", referencedColumnName = "refund_id", nullable = false)
    private Refund refund;

    @ManyToOne
    @JoinColumn(name="category_id",referencedColumnName = "category_id", nullable=true)
    private Category category;

    @ManyToOne
    @JoinColumn(name="drugCardId",referencedColumnName = "drug_card_id", nullable=true)
    private DrugCard drugCard;

    @ManyToOne
    @JoinColumn(name = "otherRefundReceiptId", referencedColumnName = "other_refund_receipt_id", nullable = true)
    private OtherRefundReceipt otherRefundReceipt;

    @Column(name = "created_at", nullable = true)
    private Date createdAt;

    @Column(name = "status")
    private int status;

}
