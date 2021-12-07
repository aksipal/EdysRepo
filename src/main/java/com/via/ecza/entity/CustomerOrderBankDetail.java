package com.via.ecza.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name="customer_order_bank_detail")
public class CustomerOrderBankDetail {

    @Id
    @SequenceGenerator(name = "sq_customer_order_bank_detail", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_customer_order_bank_detail")
    @Column(name = "customer_order_bank_detail_id")
    private Long customerOrderBankDetailId;

    @Column(name = "account_name")
    private String accountName;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "iban_no")
    private String ibanNo;

    @Column(name = "swift")
    private String swift;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_order_id", referencedColumnName = "customer_order_id", nullable = true)
    private CustomerOrder customerOrder;

}
