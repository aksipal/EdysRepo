package com.via.ecza.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;

@Data
@Getter
@Setter
@Entity
@Table(name="drug_card")
public class DrugCard {

    @Id
    @SequenceGenerator(name = "sq_drug_card", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_drug_card")
    @Column(name = "drug_card_id")
    private Long drugCardId;

    @Type(type = "text")
    @Lob
    @Column(name = "drug_name",columnDefinition="text", length=10485760)
    private String drugName;

    @Column(name = "drug_code")
    private Long drugCode;

    @Column(name = "atc_code")
    private String atcCode;

    @Type(type = "text")
    @Lob
    @Column(name = "atc_name",columnDefinition="text", length=10485760)
    private String atcName;

    @Type(type = "text")
    @Lob
    @Column(name = "drug_company",columnDefinition="text", length=10485760)
    private String drugCompany;

    @Column(name = "reference")
    private String reference;

    @Column(name = "price_protected_unprotected")
    private String priceProtectedUnprotected;

    @Column(name = "price_dec_req_code")
    private Integer priceDecReqCode;

    @Column(name = "equivalent_code")
    private Integer equivalentCode;

    @Column(name = "reference_status_code")
    private Integer referenceStatusCode;

    @Type(type = "text")
    @Lob
    @Column(name = "active_matter",columnDefinition="text", length=10485760)
    private String activeMatter;

    @Column(name = "unit_quantity")
    private String unitQuantity;

    @Column(name = "unit_type")
    private String unitType;

    @Column(name = "package_quantity_size")
    private String packageQuantitySize;

    @Column(name = "recipe_type")
    private String recipeType;

    @Column(name = "imported_manufactured")
    private String importedManufactured;

    @Column(name = "source_country")
    private String sourceCountry;

    @Column(name = "source_country_for_calculation")
    private String sourceCountryForCalculation;

    @Column(name = "date_of_change")
    private Date dateOfChange;

    @Column(name = "validity_date")
    private Date validityDate;

    @Column(name = "type_of_change_made_in_list")
    private String typeOfChangeMadeInList;

    @Type(type = "text")
    @Lob
    @Column(name = "explanation", columnDefinition="text", length=10485760)
    private String explanation;

    @Type(type = "text")
    @Lob
    @Column(name = "explanation_of_all_transactions" , columnDefinition="text", length=10485760)
    private String explanationOfAllTransactions;

    @Type(type = "text")
    @Lob
    @Column(name = "pre_decision_fdk_increases",columnDefinition="text", length=10485760)
    private String preDecisionFdkIncreases;

    @Type(type = "text")
    @Lob
    @Column(name = "after_decision_fdk_increases",columnDefinition="text", length=10485760)
    private String afterDecisionFdkIncreases;

    @Column(name = "its_information")
    private Integer itsInformation;

    @Column(name = "company_gln")
    private String companyGln;

    @Column(name = "company_tax_no")
    private String companyTaxNo;

    @Column(name = "drug_vat")
    private Double drugVat;

    @Column(name = "status")
    private int status;

    @Column(name = "is_active")
    private Boolean isActive;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "drugCard")
    @EqualsAndHashCode.Exclude private Price price;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "drugCard")
    @EqualsAndHashCode.Exclude private Discount discount;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "drugCard")
    private DrugBoxProperties drugBoxProperties;



}