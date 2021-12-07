package com.via.ecza.entity;

import com.via.ecza.entity.enumClass.CheckingCardType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;


@Data
@Getter
@Setter
@Entity
@Table(name="checking_card")
public class CheckingCard {

    @Id
    @SequenceGenerator(name = "sq_checking_card", initialValue = 3, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_checking_card")
    @Column(name = "checking_card_id")
    private Long checkingCardId;

    @Column(name = "checking_card_name")
    private String checkingCardName;

    @OneToMany(mappedBy = "checkingCard")
    private List<Account> accounts;

    @OneToMany(mappedBy = "checkingCard")
    private List<AccountActivity> accountActivities;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "countryid", referencedColumnName = "country_id", nullable = true)
    private Country country;

    @Column(name = "city")
    private String city;

    @Column(name = "sales_representative")
    private String salesRepresentative;

    @Enumerated(EnumType.STRING)
    @Column(name="type", nullable = false)
    @NotEmpty
    @NotNull
    private CheckingCardType type;

    @Type(type = "text")
    @Lob
    @Column(name = "address")
    private String address;

    @Column(name = "tax_office")
    private String taxOffice;

    @Column(name = "tax_identification_number")
    private Long taxIdentificationNumber;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "fax_number")
    private String faxNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "created_at")
    private Date createdAt;

    //Bakiye
    @Column(name = "balance")
    private Double balance;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "userid", referencedColumnName = "user_id", nullable = true)
    private User user;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "supplier_id")
    private Long supplierId;

    //mersis
    @Column(name = "crs_no")
    private String crsNo;

}
