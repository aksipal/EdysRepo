package com.via.ecza.entity;

import com.via.ecza.entity.enumClass.BoxSize;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@Getter
@Setter
@Entity
@Table(name="pre_logistic_calculation")
public class PreLogisticCalcuation {

    @Id
    @SequenceGenerator(name = "sq_pre_logistic_calculation", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_pre_logistic_calculation")
    @Column(name = "pre_logistic_calculation_id")
    private Long preLogisticCalculationId;

    @ManyToOne
    @JoinColumn(name = "customer_order_id", referencedColumnName = "customer_order_id", nullable = false)
    private CustomerOrder customerOrder;

    @Enumerated(EnumType.ORDINAL)
    @Column(name="box_size")
    @NotEmpty
    @NotNull
    private BoxSize boxSize;

//    @Column(name = "box_volume")
//    private Long boxVolume;

    @Column(name = "total_drug_count")
    private Long totalDrugCount;


    @Column(name = "empty_box_volume")
    private Long emptyBoxVolume;

    @Column(name = "total_box_volume")
    private Long totalBoxVolume;


    //Kutu Ağırlığı
    @Column(name = "total_box_weight")
    private Double totalBoxWeight;

    @Column(name = "created_at")
    private Date createdAt;

    private int status;
}
