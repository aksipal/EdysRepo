package com.via.ecza.entity;

import com.via.ecza.entity.enumClass.AccountType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;


@Data
@Getter
@Setter
@Entity
@Table(name="account")
public class Account {

    @Id
    @SequenceGenerator(name = "sq_account_id", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_account_id")
    @Column(name = "account_id")
    private Long accountId;

    @ManyToOne
    @JoinColumn(name="checking_card_id",referencedColumnName = "checking_card_id", nullable=true)
    private CheckingCard checkingCard;

    @Column(name = "account_name")
    private String accountName;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "iban_no")
    private String ibanNo;

    @Column(name = "swift_no")
    private String swiftNo;

    @Enumerated(EnumType.STRING)
    @Column(name="currency_type")
    @NotEmpty
    @NotNull
    private CurrencyType swiftType;

    @Column(name = "account_no")
    private String accountNo;

    @Column(name = "branch_no")
    private String branchNo;

    @Column(name = "branch_city")
    private String branchCity;

    @Column(name = "branch_district")
    private String branchDistrict;

    @Column(name = "branch_name")
    private String branchName;

    @Column(name = "bank_supervisor")
    private String bankSupervisor;

    @Column(name = "note")
    private String note;

    @Column(name = "created_at")
    private Date createdAt;



    @Enumerated(EnumType.STRING)
    @Column(name="account_type", nullable = true)
    private AccountType accountType;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "country_id", referencedColumnName = "country_id", nullable = true)
    private Country country;

    @ManyToOne
    @JoinColumn(name="category_id",referencedColumnName = "category_id", nullable=true)
    private Category category;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "userid", referencedColumnName = "user_id", nullable = true)
    private User user;

    @Column(name = "status")
    private int status;

}
