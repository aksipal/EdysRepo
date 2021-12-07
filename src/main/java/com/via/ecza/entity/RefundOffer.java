package com.via.ecza.entity;

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
@Table(name = "refund_offer")
public class RefundOffer {


    @Id
    @SequenceGenerator(name = "sq_refund_offer", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_refund_offer")
    @Column(name = "refund_offer_id")
    private Long refundOfferId;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "expiration_date")
    private Date expirationDate;

    @Column(name = "total_price")
    private Double totalPrice;

    @Column(name = "unit_price")
    @NotNull
    private Float unitPrice;

    @Column(name = "totality")
    @NotNull
    private Long totality;

    @Column(name = "offered_totality")
    @NotNull
    private Long offeredTotality;

    @Column(name = "offered_total_price")
    private Double offeredTotalPrice;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "drugCardId", referencedColumnName = "drug_card_id", nullable = true)
    private DrugCard drugCard;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "supplierId", referencedColumnName = "supplier_id")
    @NotEmpty
    @NotNull
    private Supplier supplier;

    
    private String refundNote;




    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "refund_offer_status_id", referencedColumnName = "refund_offer_status_id", nullable = false)
    private RefundOfferStatus refundOfferStatus;

    //fatura kime kesilecek bilgisi - (Liva Ekip ex-im gibi)
    @Column(name = "other_company_id")
    private Long otherCompanyId;


}
