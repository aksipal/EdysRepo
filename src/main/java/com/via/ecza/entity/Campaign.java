package com.via.ecza.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.Date;

@Data
@Entity
@Table(name="campaign")
public class Campaign {

    @Id
    @SequenceGenerator(name = "sq_campaign", initialValue = 2, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_campaign")
    @Column(name = "campaign_id")
    private Long campaignId;

    @Temporal(TemporalType.DATE)
    @Column(name = "campaign_start_date")
    private Date campaignStartDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "campaign_end_date")
    private Date campaignEndDate;

    @Column(name = "createdDate")
    private Date createdDate;

    @Column(name = "campaign_unit_price")
    private Double campaignUnitPrice;

    @Column(name = "campaign_unit_cost")
    private Double campaignUnitCost;

    @ManyToOne
    @JoinColumn(name = "drugCardId", referencedColumnName = "drug_card_id", nullable = false)
    private DrugCard drugCard;

    @Enumerated(EnumType.STRING)
    @Column(name="currency_type")
    private CurrencyType currencyType;

    @Column(name = "instution_discount")
    private Double instutionDiscount;

    @Column(name = "campaign_unit_price_currency")
    private Double campaignUnitPriceCurrency;

    @Column(name = "currency_fee")
    private Double currencyFee;

    @Column(name = "campaign_unit_price_excluding_vat")
    private Double campaignUnitPriceExcludingVat;

    @Column(name = "depot_sale_price_excluding_vat")
    private Double depotSalePriceExcludingVat;

    @Column(name = "mf1")
    private int mf1;

    @Column(name = "mf2")
    private int mf2;

    @Column(name = "profit")
    private int profit;

    @Column(name = "status")
    private int status;

    @Column(name = "is_deleted")
    private int isDeleted;

    @Column(name = "vat")
    private Double vat;

}
