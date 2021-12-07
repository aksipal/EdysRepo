package com.via.ecza.entity;

import com.sun.istack.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
@Getter
@Setter
@Entity
@Table(name="box")
public class Box {
    @Id
    @SequenceGenerator(name = "sq_box", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_box")
    @Column(name = "box_id")
    private Long boxId;

    @Column(name = "box_no")
    private String boxNo;

    @Column(name = "customer_box_no")
    private String customerBoxNo;

    @Column(name = "box_weight")
    private Double boxWeight;

    @Column(name = "exact_box_weight")
    private Double exactBoxWeight;

    @Column(name = "status")
    private int status;

    @Column(name = "created_date")
    private Date createdDate;

    @ManyToOne
    @JoinColumn(name="customer_order_id",referencedColumnName = "customer_order_id")
    private CustomerOrder customerOrder;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "box_type_id", referencedColumnName = "box_type_id", nullable = false)
    private BoxType boxType;

    @OneToMany(mappedBy = "box")
    private List<BoxDrugList> boxDrugList;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "userid", referencedColumnName = "user_id", nullable = true)
    private User user;

}
