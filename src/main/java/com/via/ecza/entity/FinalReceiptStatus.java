package com.via.ecza.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Data
@Getter
@Setter
@Entity
@Table(name="final_receipt_status")
public class FinalReceiptStatus {

    @Id
    @SequenceGenerator(name = "sq_final_receipt_status", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_final_receipt_status")
    @Column(name = "final_receipt_status_id")
    private Long finalReceiptStatusId;

    @Column(name = "status_name")
    private String statusName;

    @Column(name = "explanation")
    private String explanation;
}
