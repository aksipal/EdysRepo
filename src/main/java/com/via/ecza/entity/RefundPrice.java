package com.via.ecza.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "refund_price")
public class RefundPrice {

    @Id
    @SequenceGenerator(name = "sq_refund_price", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_refund_price")
    @Column(name = "refund_price_id")
    private Long refundPriceId;

    @Column(name = "vat")
    private Double vat;

    @Column(name = "vat_sum")
    private Double vatSum;

    @Column(name = "account_total_price")
    private Double accountTotalPrice;

    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "refund_id", referencedColumnName = "refund_id", nullable = false)
    private Refund refund;
}
