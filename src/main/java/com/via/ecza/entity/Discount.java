package com.via.ecza.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name="discount")
public class Discount {
    @Id
    @SequenceGenerator(name = "sq_discount", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_discount")
    @Column(name = "discount_id")
    private Long discountId;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "drugCardId", referencedColumnName = "drug_card_id", nullable = false)
    private DrugCard drugCard;

    @Column(name = "general_discount")
    @NotEmpty
    @NotNull
    private float generalDiscount;

    @Column(name = "instution_discount")
    @NotEmpty
    @NotNull
    private float instutionDiscount;

    @Column(name = "surplus_discount")
    @NotEmpty
    @NotNull
    private String surplusDiscount;
}
