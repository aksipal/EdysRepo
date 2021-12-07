package com.via.ecza.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;
import java.util.List;


@Data
@Entity
@Table(name = "receipt")
public class Receipt {

    @Id
    @SequenceGenerator(name = "sq_receipt_id", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_receipt_id")
    @Column(name = "receipt_id")
    private Long receiptId;

    @Column(name = "receipt_no")
    private String receiptNo;

    @JsonIgnore
    @OneToMany(mappedBy = "receipt")
    private List<CustomerSupplyOrder> customerSupplyOrders;

    @OneToMany(mappedBy = "receipt")
    private List<Refund> refunds;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "supplier_id", referencedColumnName = "supplier_id", nullable = true)
    private Supplier supplier;

    @Column(name = "invoice_no")
    private String invoiceNo;

    @Column(name = "invoice_date")
    private Date invoiceDate;


    @Column(name = "created_at", nullable = true)
    private Date createdAt;

    @Column(name = "due_date", nullable = true)
    private Date dueDate;

    @Column(name = "dispatch_no")
    private String dispatchNo;

    @Type(type = "text")
    @Lob
    @Column(name = "receipt_note", nullable = true)
    private String receiptNote;

    @Column(name = "dispatch_date")
    private Date dispatchDate;

    @Type(type = "text")
    @Lob
    @Column(name = "invoice_note", nullable = true)
    private String invoiceNote;


//    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
//    @JoinColumn(name = "invoice_status_id", referencedColumnName = "invoice_status_id", nullable = false)
//    private InvoiceStatus invoiceStatus;

//    @ManyToOne
//    @JoinColumn(name="final_receipt_id",referencedColumnName = "final_receipt_id", nullable=true)
//    private FinalReceipt finalReceipt;

    @Enumerated(EnumType.STRING)
    @Column(name = "receipt_type")
    private ReceiptType receiptType;

    @OneToOne
    @JoinColumn(name = "receipt_status_id", referencedColumnName = "receipt_status_id")
    private ReceiptStatus receiptStatus;

    @Column(name = "status")
    private int status;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "invoice_id", referencedColumnName = "invoice_id", nullable = true)
    private Invoice invoice;

    @OneToMany(mappedBy = "receipt")
    private List<ReceiptContent> receiptContents;


}
