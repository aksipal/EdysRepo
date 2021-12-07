package com.via.ecza.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@Entity
@ToString
@Table(name = "pre_customer_order_drugs")
public class PreCustomerOrderDrugs {

    @Id
    @SequenceGenerator(name = "sq_pre_customer_order_drug", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_pre_customer_order_drug")
    @Column(name = "pre_customer_order_drug_id")
    private Long preCustomerOrderDrugId;

    @Column(name = "total_quantity")
    @NotEmpty
    @NotNull
    private Long totalQuantity;

    @Column(name = "status")
    private int status;

    @Column(name = "expiration_date")
    @NotEmpty
    @NotNull
    private Date expirationDate;

    @Lob
    @Type(type = "text")
    @Column(name = "customer_order_drug_note", length = 3000)
    private String customerOrderDrugNote;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "drugCardId", referencedColumnName = "drug_card_id", nullable = true)
    private DrugCard drugCard;

    @ManyToOne
    @JoinColumn(name = "customer_order_id", referencedColumnName = "customer_order_id", nullable = true)
    private CustomerOrder customerOrder;

    @OneToOne
    @JoinColumn(name="user_id",referencedColumnName = "user_id", nullable=true)
    private User user;

}