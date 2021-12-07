package com.via.ecza.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;


@Data
@Entity
@Table(name="customer_receipt_content")
public class CustomerReceiptContent {

    @Id
    @SequenceGenerator(name = "sq_customer_receipt_content_id", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_customer_receipt_content_id")
    @Column(name = "customer_receipt_content_id")
    private Long customerReceiptContentId;

    @ManyToOne
    @JoinColumn(name="category_id",referencedColumnName = "category_id", nullable=true)
    private Category category;

    @ManyToOne
    @JoinColumn(name="drugCardId",referencedColumnName = "drug_card_id", nullable=true)
    private DrugCard drugCard;

    @ManyToOne
    @JoinColumn(name = "customerReceiptId", referencedColumnName = "customer_receipt_id", nullable = true)
    private CustomerReceipt customerReceipt;

    @Column(name = "created_at", nullable = true)
    private Date createdAt;

    @Column(name = "status")
    private int status;

    @ManyToOne
    @JoinColumn(name = "customerOrderId", referencedColumnName = "customer_order_id", nullable = true)
    private CustomerOrder customerOrder;

}
