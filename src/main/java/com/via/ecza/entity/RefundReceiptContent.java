package com.via.ecza.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;


@Data
@Entity
@Table(name="refund_receipt_content")
public class RefundReceiptContent {

    @Id
    @SequenceGenerator(name = "sq_refund_receipt_content_id", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_refund_receipt_content_id")
    @Column(name = "sq_refund_receipt_content_id")
    private Long receiptContentId;

    @ManyToOne
    @JoinColumn(name="category_id",referencedColumnName = "category_id", nullable=true)
    private Category category;

    @ManyToOne
    @JoinColumn(name="drugCardId",referencedColumnName = "drug_card_id", nullable=true)
    private DrugCard drugCard;

    @ManyToOne
    @JoinColumn(name = "refundReceiptId", referencedColumnName = "refund_receipt_id", nullable = true)
    private RefundReceipt refundReceipt;

    @Column(name = "created_at", nullable = true)
    private Date createdAt;

    @Column(name = "status")
    private int status;

}
