package com.via.ecza.entity;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Data
@Getter
@Setter
@Entity
@Table(name="pre_refund_status")
public class PreRefundStatus {

    @Id
    @SequenceGenerator(name = "sq_pre_refund_status", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_pre_refund_status")
    @Column (name= "pre_refund_status_id")
    private Long preRefundStatusId;

    private String statusName;
    private String explanation;
}
