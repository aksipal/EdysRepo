package com.via.ecza.entity;

import com.sun.istack.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

@Data
@Getter
@Setter
@Entity
@Table(name = "supplier_supervisor")
public class SupplierSupervisor {
    @Id
    @SequenceGenerator(name = "sq_supplier_supervisor", initialValue = 2, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_supplier_supervisor")
    @Column(name = "supplier_supervisor_id")
    private Long supplierSupervisorId;

    @Column(name = "name")
    @NotEmpty
    @NotNull
    private String name;

    @Column(name = "surname")
    @NotEmpty
    @NotNull
    private String surname;

    @Column(name = "email")
    @NotEmpty
    @NotNull
    private String email;

    @Column(name = "phone_number")
    @NotEmpty
    @NotNull
    private String phoneNumber;

    @Column(name = "job_title")
    private String jobTitle;

    @ManyToOne
    @JoinColumn(name = "supplierId", referencedColumnName = "supplier_id")
    @NotEmpty
    @NotNull
    private Supplier supplier;

    @Column(name = "status")
    private int status;

}
