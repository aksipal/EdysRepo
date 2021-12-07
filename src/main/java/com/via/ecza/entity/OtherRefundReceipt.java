package com.via.ecza.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;


@Data
@Entity
@Table(name="other_refund_receipt")
public class OtherRefundReceipt {

    @Id
    @SequenceGenerator(name = "sq_other_refund_receipt", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_other_refund_receipt")
    @Column(name = "other_refund_receipt_id")
    private Long otherRefundReceiptId;

    @Column(name = "other_refund_receipt_no")
    private String otherRefundReceiptNo;

//    @OneToMany(mappedBy = "refundReceipt")
//    private List<Refund> refunds;


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


    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "invoice_id", referencedColumnName = "invoice_id", nullable = true)
    private Invoice invoice;

//    @JsonIgnore
//    @OneToOne
//    @JoinColumn(name = "previous_invoice_id", referencedColumnName = "invoice_id", nullable = true)
//    private Invoice previousInvoice;

    @OneToMany(mappedBy = "otherRefundReceipt")
    private List<OtherRefundPrice> otherRefundPrices;


}
