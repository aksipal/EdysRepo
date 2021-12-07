package com.via.ecza.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Data
@Getter
@Setter
@Entity
@Table(name = "supplier_offer_status")
public class SupplierOfferStatus {

    @Id
    @SequenceGenerator(name = "sq_supplier_offer_status", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_supplier_offer_status")
    @Column(name = "supplier_offer_status_id")
    private Long supplierOfferStatusId;

    private String statusName;
    private String explanation;


}
