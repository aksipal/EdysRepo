package com.via.ecza.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;


@Data
@Getter
@Setter
@Entity
@Table(name="accounting_code")
public class AccountingCode {

    @Id
    @SequenceGenerator(name = "sq_accounting_code_id", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_accounting_code_id")
    @Column(name = "accounting_code_id")
    private Long accountingCodeId;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "reverse_working_account")
    private Boolean reverseWorkingAccount;

    @Column(name = "created_date")
    private Date createdDate;

    @Column(name = "status")
    private int status;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "accountingCode")
    private AccountActivity accountActivity;

    @ManyToOne
    @JoinColumn(name="category_id",referencedColumnName = "category_id")
    private Category category;

}
