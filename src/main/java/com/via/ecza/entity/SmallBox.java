package com.via.ecza.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@Getter
@Setter
@Entity
@Table(name="small_box")
public class SmallBox {
    @Id
    @SequenceGenerator(name = "sq_small_box", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_small_box")
    @Column(name = "small_box_id")
    private Long smallBoxId;

    @Column(name = "small_box_no")
    private String smallBoxNo;

    @OneToMany(mappedBy = "smallBox")
    private List<BoxDrugList> boxDrugList;

    @Column(name = "created_date")
    private Date createdDate;

    @Column(name = "status")
    private int status;

    @ManyToOne
    @JoinColumn(name="customer_order_id",referencedColumnName = "customer_order_id")
    private CustomerOrder customerOrder;

//    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
//    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = true)
//    private User user;
}
