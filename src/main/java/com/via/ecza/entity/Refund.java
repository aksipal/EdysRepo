package com.via.ecza.entity;

import com.sun.istack.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.Date;


@Data
@Getter
@Setter
@Entity
@Table(name = "refund")
public class Refund {

    @Id
    @SequenceGenerator(name = "sq_refund", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_refund")
    @Column(name = "refund_id")
    private Long refundId;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "expiration_date")
    private Date expirationDate;

    @Column(name = "total_price")
    private Double totalPrice;

    @Column(name = "unit_price")
    @NotNull
    private Float unitPrice;

    @Column(name = "totality")
    @NotNull
    private Long totality;


    @Column(name = "refund_order_no")
    private String refundOrderNo;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "drugCardId", referencedColumnName = "drug_card_id", nullable = true)
    private DrugCard drugCard;

    @ManyToOne
    @JoinColumn(name = "supplierId", referencedColumnName = "supplier_id")
    @NotEmpty
    @NotNull
    private Supplier supplier;

    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "refund_status_id", referencedColumnName = "refund_status_id", nullable = false)
    private RefundStatus refundStatus;

    @ManyToOne
    @JoinColumn(name = "receiptId", referencedColumnName = "receipt_id", nullable = true)
    private Receipt receipt;

    @Column(name = "acceptance_date")
    private Date acceptanceDate;

    @OneToOne( mappedBy = "refund")
    @EqualsAndHashCode.Exclude private RefundPrice refundPrice;

//    @OneToOne( mappedBy = "refund")
//    private OtherRefundPrice otherRefundPrice;


    @ManyToOne
    @JoinColumn(name = "refundReceiptId", referencedColumnName = "refund_receipt_id", nullable = true)
    private RefundReceipt refundReceipt;

    //fatura kime kesilecek bilgisi - (Liva Ekip ex-im gibi)
    @Column(name = "other_company_id")
    private Long otherCompanyId;

}
