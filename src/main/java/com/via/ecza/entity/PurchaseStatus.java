package com.via.ecza.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Data
@Getter
@Setter
@Entity
@Table(name = "purchase_status")
public class PurchaseStatus {

    @Id
    @SequenceGenerator(name = "sq_purchase_status", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_purchase_status")
    @Column(name = "purchase_status_id")
    private Long purchaseStatusId;

    private String statusName;
    private String explanation;
}
