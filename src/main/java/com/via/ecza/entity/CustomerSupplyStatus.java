package com.via.ecza.entity;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;


@Data
@Getter
@Setter
@Entity
@Table(name = "customer_supply_status")
public class CustomerSupplyStatus {

    @Id
    @SequenceGenerator(name = "sq_customer_supply_status", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_customer_supply_status")
    @Column(name = "customer_supply_status_id")
    private Long customerSupplyStatusId;

    private String statusName;
    private String explanation;
}
