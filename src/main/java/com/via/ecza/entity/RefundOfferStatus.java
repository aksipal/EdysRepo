package com.via.ecza.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;


@Data
@Getter
@Setter
@Entity
@Table(name = "refund_offer_status")
public class RefundOfferStatus {

    @Id
    @SequenceGenerator(name = "sq_refund_offer_status", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_refund_offer_status")
    @Column(name = "refund_offer_status_id")
    private Long refundOfferStatusId;

    private String statusName;
    private String explanation;
}
