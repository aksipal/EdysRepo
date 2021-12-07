package com.via.ecza.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name="customer_order_status_history")
public class CustomerOrderStatusHistory {

    @Id
    @SequenceGenerator(name = "sq_customer_order_status_history", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_customer_order_status_history")
    @Column(name= "order_status_history_id")
    private Long orderStatusHistoryId;

    @Column(name= "status_name")
    private String statusName;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_order_status_id", referencedColumnName = "order_status_id", nullable = true, unique = false)
    private CustomerOrderStatus customerOrderStatus;

    @ManyToOne
    @JoinColumn(name = "customer_order_id", referencedColumnName = "customer_order_id", nullable = true)
    private CustomerOrder customerOrder;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date")
    private Date createdDate;
}
