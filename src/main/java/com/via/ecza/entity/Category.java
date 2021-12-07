package com.via.ecza.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name="category")
public class Category {

    @Id
    @SequenceGenerator(name = "sq_category_id", initialValue = 267, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_category_id")
    @Column(name = "category_id")
    private Long categoryId;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name="category_parent_id",referencedColumnName = "category_id")
    private Category parentCategory;

    @Column(name = "name")
    private String name;

    @Column(name = "code")
    private String code;


    @Column(name = "code_value")
    private String codeValue;

    @Column(name = "created_date")
    private Date createdDate;

    @Column(name = "status")
    private int status;

    @OneToMany(mappedBy = "category")
    private List<AccountingCode> accountingCodes;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "checking_card_id", referencedColumnName = "checking_card_id", nullable = true)
    private CheckingCard checkingCard;

}
