package com.via.ecza.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;


@Data
@Getter
@Setter
@Entity
@Table(name = "refund_status")
public class RefundStatus {
    @Id
    @SequenceGenerator(name = "sq_refund_status", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_refund_status")
    @Column(name = "refund_status_id")
    private Long refundStatusId;

    private String statusName;
    private String explanation;
}
