package com.via.ecza.entity;


import com.sun.istack.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.List;

@Data
@Getter
@Setter
@Entity
@Table(name="customer")
public class Customer {

    @Id
    @SequenceGenerator(name = "sq_customer", initialValue = 2, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_customer")
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "name")
    @NotEmpty
    @NotNull
    private String name;

    @Column(name = "surname")
    @NotEmpty
    @NotNull
    private String surname;

    @Column(name = "createdDate")
    private Date createdDate;

    @Column(name = "city")
    @NotEmpty
    @NotNull
    private String city;

    @Column(name = "open_address")
    private String openAddress;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "eposta")
    private String eposta;

    @Column(name = "business_phone")
    private String businessPhone;

    @Column(name = "mobile_phone")
    private String mobilePhone;

    @Column(name = "mobile_phone_extra")
    private String mobilePhoneExtra;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "customer_fax")
    private String customerFax;

    @ManyToOne
    @JoinColumn(name="companyid",referencedColumnName = "company_id", nullable=true)
    private Company company;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "countryid", referencedColumnName = "country_id", nullable = true)
    private Country country;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "userid", referencedColumnName = "user_id", nullable = true)
    private User user;

    @OneToMany(mappedBy = "customer")
    private List<CustomerOrder> customerOrders;

    @OneToMany(mappedBy = "customer")
    private List<CustomerReceipt> customerReceipts;

    @Column(name = "status")
    private int status;

}
