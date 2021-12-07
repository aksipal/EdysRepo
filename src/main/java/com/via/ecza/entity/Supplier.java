package com.via.ecza.entity;

import com.sun.istack.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Getter
@Setter
@Entity
@Table(name = "supplier")
public class Supplier {


    @Id
    @SequenceGenerator(name = "sq_supplier", initialValue = 2, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_supplier")
    @Column(name = "supplier_id")
    private Long supplierId;

    @Column(name = "supplier_name")
    @NotEmpty
    @NotNull
    private String supplierName;

    @Column(name = "supplier_city")
    @NotEmpty
    @NotNull
    private String supplierCity;

    @Column(name = "supplier_district")
    @NotEmpty
    @NotNull
    private String supplierDistrict;

    @Column(name = "supplier_address")
    @NotEmpty
    @NotNull
    private String supplierAddress;

    @Column(name = "supplier_email")
    @NotEmpty
    @NotNull
    private String supplierEmail;

    @Column(name = "phone_number")
    @NotEmpty
    @NotNull
    private String phoneNumber;

    @Column(name = "supplier_fax")
    private String supplierFax;

    @Column(name = "supplier_tax_no")
    @NotEmpty
    @NotNull
    private String supplierTaxNo;

    @Column(name = "supplier_profit")
    @NotNull
    private Float supplierProfit;

    @Enumerated(EnumType.STRING)
    @NotNull
    @NotEmpty
    @Column(name = "supplier_type")
    private SupplierType supplierType;

    @OneToMany(mappedBy = "supplier")
    private List<SupplierSupervisor> supplierSupervisors;

    @OneToMany(mappedBy = "supplier")
    private List<CustomerSupplyOrder> customerSupplyOrders;

    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "userid", referencedColumnName = "user_id", nullable = true)//Sonrasında nullable false yap
    private User user;

    @OneToMany(mappedBy = "supplier")
    private List<Receipt> receipts;


    @Column(name = "status")
    private Integer status;

}
