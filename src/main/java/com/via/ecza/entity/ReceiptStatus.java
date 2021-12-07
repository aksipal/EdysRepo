package com.via.ecza.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Data
@Getter
@Setter
@Entity
@Table(name = "receipt_status")
public class ReceiptStatus {

    @Id
    @SequenceGenerator(name = "sq_receipt_status", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_receipt_status")
    @Column(name = "receipt_status_id")
    private Long receiptStatusId;

    @Column(name = "status_name")
    private String statusName;

    @Column(name = "explanation")
    private String explanation;
}
