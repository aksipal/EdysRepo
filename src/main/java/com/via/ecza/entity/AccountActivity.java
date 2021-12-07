package com.via.ecza.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.via.ecza.entity.enumClass.AccountActivityType;
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
@Table(name="account_activity")
public class AccountActivity {


    @Id
    @SequenceGenerator(name = "sq_account_activity_id", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_account_activity_id")
    @Column(name = "account_activity_id")
    private Long accountActivityId;

    //Cari Hesabı
    @OneToOne
    @JoinColumn(name="account_id",referencedColumnName = "account_id", nullable=true)
    private Account account;

    //Karşı Cari Hesabı
    @OneToOne
    @JoinColumn(name="other_account_id",referencedColumnName = "account_id", nullable=true)
    private Account otherAccount;

    //Fatura Eden
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "checking_card_id", referencedColumnName = "checking_card_id", nullable = true)
    private CheckingCard checkingCard;

    //Fatura Edilen
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "other_checking_card_id", referencedColumnName = "checking_card_id", nullable = true)
    private CheckingCard otherCheckingCard;

    @ManyToOne
    @JoinColumn(name="invoice_id",referencedColumnName = "invoice_id", nullable=true)
    private Invoice invoice;

    @Column(name = "invoice_No")
    private String invoiceNo;

    @Enumerated(EnumType.STRING)
    @Column(name="currency_type")
    @NotEmpty
    @NotNull
    private CurrencyType currencyType;

    @Enumerated(EnumType.STRING)
    @Column(name="account_activity_type")
    private AccountActivityType accountActivityType;

    @Column(name = "currency_fee")
    private Double currencyFee;

    @Column(name = "created_at")
    private Date createdAt;

    //Alacak
    @Column(name = "charge")
    private Double charge;

    //Borç
    @Column(name = "debt")
    private Double debt;

    @Column(name = "status")
    private int status;

    //Ödeme Tarihi - Vade
    @Column(name = "date_of_issue")
    private Date dateOfIssue;

    //Tahsil Edildiği Tarih
    @Column(name = "paid_day")
    private Date paidDay;

    //Belge Oluşturulma Tarihi
    @Column(name = "document_created_date")
    private Date documentCreatedDate;

    @Column(name = "bond_payer_identity_number")
    private String bondPayerIdentityNumber;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "accounting_code_id", referencedColumnName = "accounting_code_id", nullable = true)
    private AccountingCode accountingCode;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", referencedColumnName = "category_id", nullable = true)
    private Category category;

}
