package com.via.ecza.entity;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Data
@Getter
@Setter
@Entity
@Table(name="customer_order_status")
public class CustomerOrderStatus {

    @Id
    @SequenceGenerator(name = "sq_customer_order_status", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_customer_order_status")
    @Column (name= "order_status_id")
    private Long orderStatusId;

    @Column (name= "status_name")
    private String statusName;

    @Column (name= "explanation")
    private String explanation;
}
