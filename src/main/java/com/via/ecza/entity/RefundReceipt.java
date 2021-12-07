package com.via.ecza.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;


@Data
@Entity
@Table(name="refund_receipt")
public class RefundReceipt {

    @Id
    @SequenceGenerator(name = "sq_refund_receipt", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_refund_receipt")
    @Column(name = "refund_receipt_id")
    private Long refundReceiptId;

    @Column(name = "refund_receipt_no")
    private String refundReceiptNo;

    @OneToMany(mappedBy = "refundReceipt")
    private List<Refund> refunds;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "supplier_id", referencedColumnName = "supplier_id", nullable = true)
    private Supplier supplier;

    @Column(name = "invoice_no")
    private String invoiceNo;

    @Column(name = "invoice_date")
    private Date invoiceDate;


    @Column(name = "created_at", nullable = true)
    private Date createdAt;


    @Enumerated(EnumType.STRING)
    @Column(name = "receipt_type")
    private ReceiptType receiptType;

    @OneToOne
    @JoinColumn(name="receipt_status_id", referencedColumnName = "receipt_status_id")
    private ReceiptStatus receiptStatus;

    @Column(name = "status")
    private int status;


    @OneToOne
    @JoinColumn(name="invoice_id",referencedColumnName = "invoice_id", nullable=true)
    private Invoice invoice;

    @OneToMany(mappedBy = "refundReceipt")
    private List<RefundReceiptContent> refundReceiptContents;


}
