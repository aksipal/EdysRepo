package com.via.ecza.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Data
@Getter
@Setter
@Entity
@Table(name="price")
public class Price {


    @Id
    @SequenceGenerator(name = "sq_price", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_price")
    @Column(name = "price_id")
    private Long priceId;

    @Column(name = "drug_barcode")
    private Long drugBarcode;


    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "drugCardId", referencedColumnName = "drug_card_id", nullable = false)
    private DrugCard drugCard;



    @Column(name = "real_source_price")
    private Double realSourcePrice;

    @Column(name = "real_source_price_for_calculation")
    private Double realSourcePriceForCalculation;

    @Column(name = "source_price")
    private Double sourcePrice;

    @Column(name = "source_country_for_calculation")
    private String sourceCountryForCalculation;

    @Column(name = "sale_price_type")
    private Integer salePriceType;

    @Column(name = "sale_price_to_depot_excluding_vat")
    private Double salePriceToDepotExcludingVat;

    @Column(name = "depot_sale_price_excluding_vat")
    private Double depotSalePriceExcludingVat;

    @Column(name = "pharmacist_sale_price_excluding_vat")
    private Double pharmacistSalePriceExcludingVat;

    @Column(name = "retail_sale_price_including_vat")
    private Double retailSalePriceIncludingVat;

    @Column(name = "date_of_change")
    private Date dateOfChange;

    @Column(name = "validity_date")
    private Date validityDate;


    private int status;


}