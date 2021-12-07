package com.via.ecza.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name="customer_order_shipping_adress")
public class CustomerOrderShippingAdress {

    @Id
    @SequenceGenerator(name = "sq_customer_order_shipping_adress", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_customer_order_shipping_adress")
    @Column(name = "customer_order_shipping_adress")
    private Long customerOrderShippingAdressId;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "full_address")
    private String fullAddress;

    @Column(name = "city")
    private String city;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "country_id", referencedColumnName = "country_id", nullable = false)
    private Country country;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "createdDate")
    private Date createdDate;


    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_order_id", referencedColumnName = "customer_order_id", nullable = true)
    private CustomerOrder customerOrder;

}
