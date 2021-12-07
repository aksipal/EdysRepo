package com.via.ecza.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name="customer_receipt")
public class CustomerReceipt {

    @Id
    @SequenceGenerator(name = "sq_customer_receipt_id", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_customer_receipt_id")
    @Column(name = "customer_receipt_id")
    private Long customerReceiptId;

    @Column(name = "receipt_no")
    private String receiptNo;

    @OneToMany(mappedBy = "customerReceipt")
    private List<CustomerOrderDrugs> customerOrderDrugs;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", referencedColumnName = "customer_id", nullable = true)
    private Customer customer;

    @Column(name = "created_at", nullable = true)
    private Date createdAt;



    @Enumerated(EnumType.STRING)
    @Column(name = "receipt_type")
    private ReceiptType receiptType;

    @OneToOne
    @JoinColumn(name="receiptStatus", referencedColumnName = "receipt_status_id")
    private ReceiptStatus receiptStatus;

    @Column(name = "status")
    private int status;

//    @Column(name = "invoice_id")
//    private Long invoiceId;

    @OneToOne
    @JoinColumn(name="invoice_id",referencedColumnName = "invoice_id", nullable=true)
    private Invoice invoice;

    @OneToMany(mappedBy = "customerReceipt")
    private List<CustomerReceiptContent> customerReceiptContents;
}
