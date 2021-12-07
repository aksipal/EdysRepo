package com.via.ecza.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.istack.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.Date;

@Data
@Getter
@Setter
@Entity
@Table(name="other_company")
public class OtherCompany {

    @Id
    @SequenceGenerator(name = "sq_other_company", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_other_company")
    @Column(name = "other_company_id")
    private Long otherCompanyId;

    @Column(name = "other_company_name")
    @NotEmpty
    @NotNull
    private String otherCompanyName;

    @Column(name = "created_date")
    private Date createdDate;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "checking_card_id", referencedColumnName = "checking_card_id", nullable = true)
    private CheckingCard checkingCard;

    private int status;

}
