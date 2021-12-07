package com.via.ecza.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;


@Data
@Entity
@Table(name="receipt_content")
public class ReceiptContent {

    @Id
    @SequenceGenerator(name = "sq_receipt_content_id", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_receipt_content_id")
    @Column(name = "sq_receipt_content_id")
    private Long receiptContentId;

    @ManyToOne
    @JoinColumn(name="category_id",referencedColumnName = "category_id", nullable=true)
    private Category category;

    @ManyToOne
    @JoinColumn(name="drugCardId",referencedColumnName = "drug_card_id", nullable=true)
    private DrugCard drugCard;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "receiptId", referencedColumnName = "receipt_id", nullable = true)
    private Receipt receipt;

    @Column(name = "created_at", nullable = true)
    private Date createdAt;

    @Column(name = "status")
    private int status;

}
