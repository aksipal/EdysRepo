package com.via.ecza.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Data
@Getter
@Setter
@Entity
@Table(name = "supply_order_price")
public class SupplyOrderPrice {

    @Id
    @SequenceGenerator(name = "sq_supply_order_price", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_supply_order_price")
    @Column(name = "supply_order_price_id")
    private Long supplyOrderPriceId;

    @Column(name = "vat")
    private Double vat;

    @Column(name = "vat_sum")
    private Double vatSum;

    //KDV YOK
    @Column(name = "account_total_price")
    private Double accountTotalPrice;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_supply_order_id", referencedColumnName = "customer_supply_order_id", nullable = true)
    private CustomerSupplyOrder supplyOrder;

}
