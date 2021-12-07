package com.via.ecza.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;


@Data
@Getter
@Setter
@Entity
@Table(name="invoice_type")
public class InvoiceType {

    @Id
    @SequenceGenerator(name = "sq_invoice_type", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_invoice_type")
    @Column(name = "invoice_type_id")
    private Long invoiceTypeId;

    @Column(name = "invoice_type")
    private String invoiceType;

    private int status;


}
