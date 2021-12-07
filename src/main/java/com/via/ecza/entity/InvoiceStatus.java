package com.via.ecza.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;


@Data
@Getter
@Setter
@Entity
@Table(name="invoice_status")
public class InvoiceStatus {

    @Id
    @SequenceGenerator(name = "sq_invoice_status", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_invoice_status")
    @Column(name = "invoice_status_id")
    private Long invoiceStatusId;

    @Column(name = "status_name")
    private String statusName;

    @Column(name = "explanation")
    private String explanation;

}
