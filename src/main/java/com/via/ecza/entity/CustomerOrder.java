package com.via.ecza.entity;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;


@Data
@Getter
@Setter
@Entity(name="customer_order")
@Table(name="customer_order")
public class CustomerOrder {

    @Id
    @SequenceGenerator(name = "sq_customer_order", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_customer_order")
    @Column(name = "customer_order_id")
    private Long customerOrderId;

    @Column(name = "customer_order_no")
    private String customerOrderNo;

    @Column(name = "payment_terms")
    private String paymentTerms;

    @Column(name = "delivery_terms")
    private String deliveryTerms;

    @Column(name = "lead_time")
    private String leadTime;

    @Column(name = "additional_details")
    private String additionalDetails;

    @Column(name = "order_date")
    private Date orderDate;

    @Column(name = "created_date")
    private Date createdDate;
;
    @Column(name = "freight_cost_tl")
    private Double freightCostTl;

    @Enumerated(EnumType.STRING)
    @Column(name="currency_type")
    @NotEmpty
    @NotNull
    private CurrencyType currencyType;

    @Column(name = "currency_fee")
    private Double currencyFee;

    @Column(name = "pre_freight_cost_tl")
    private Double preFreightCostTl;

    @Type(type = "text")
    @Lob
    @Column(name = "customer_order_note", nullable = true)
    private String customerOrderNote;

    @Type(type = "text")
    @Lob
    @Column(name = "purchase_order_note",  nullable = true)
    private String purchaseOrderNote;

    @ManyToOne
    @NotNull
    @NotEmpty
    @JoinColumn(name="customer_id", referencedColumnName = "customer_id", nullable = false)
    private Customer customer;

    //@OneToMany(targetEntity = OrderDrugs.class)
    @OneToMany(mappedBy = "customerOrder")
    private List<CustomerOrderDrugs> customerOrderDrugs;

    @OneToMany(mappedBy = "customerOrder")
    private List<CustomerOrderLogisticDocument> customerOrderLogisticDocument;

    @OneToMany(mappedBy = "customerOrder")
    private List<PurchaseOrderDrugs> purchaseOrderDrugs;

    @OneToMany(mappedBy = "customerOrder")
    private List<Box> boxes;

    @OneToMany(mappedBy = "customerOrder")
    private List<SmallBox> smallBoxes;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = true)
    private User user;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "order_status_id", referencedColumnName = "order_status_id", nullable = false)
    private CustomerOrderStatus orderStatus;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "customerOrder")
    private CustomerOrderBankDetail customerOrderBankDetail;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "customerOrder")
    private CustomerOrderShippingAdress customerOrderShippingAdress;

    @ManyToOne
    @JoinColumn(name="companyid",referencedColumnName = "company_id", nullable=true)
    private Company company;

    @Column(name = "status")
    private int status;

    @Column(name = "pre_freigh_cost")
    private Double preFreighCost;



    @Column(name = "order_status_history")
    private Long orderStatusHistory;

    @Column(name = "logistic_status", nullable = true)
    private int logisticStatus;

//    @OneToMany(mappedBy = "customerOrder")
//    private List<LogisticCalcuation> logisticCalcuations;

    @OneToMany(mappedBy = "customerOrder")
    private List<PreLogisticCalcuation> preLogisticCalcuations;

    @OneToMany(mappedBy = "customerOrder")
    private List<CustomerOrderStatusHistory> orderStatusHistories;

    @OneToMany(mappedBy = "customerOrder")
    private List<CustomerReceiptContent> customerReceiptContents;

}
