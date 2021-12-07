package com.via.ecza.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;

@Data
@Getter
@Setter
@Entity
@Table(name="logistic_calculation")
public class LogisticCalculation {

    @Id
    @SequenceGenerator(name = "sq_logistic_calculation", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_logistic_calculation")
    @Column(name = "logistic_calculation_id")
    private Long logisticCalculationId;

    @OneToOne
    @JoinColumn(name = "customer_order_id", referencedColumnName = "customer_order_id", nullable = false)
    private CustomerOrder customerOrder;

    @Column(name = "total_weight")
    private Double totalWeight;

    @Column(name = "total_volume")
    private Double totalVolume;

    @Column(name = "total_cost")
    private Double totalCost;

    @Column(name = "total_box_count")
    private Integer totalBoxCount;

    @Column(name = "created_at")
    private Date createdAt;

    private int status;

}
