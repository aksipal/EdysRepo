package com.via.ecza.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
@Data
@Getter
@Setter
@Entity
@Table(name="depot")
public class Depot {
    @Id
    @SequenceGenerator(name = "sq_depot", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_depot")
    @Column(name = "depot_id")
    private Long depotId;

    @ManyToOne
    @JoinColumn(name="drugCardId",referencedColumnName = "drug_card_id")
    private DrugCard drugCard;

    @ManyToOne
    @JoinColumn(name="customerOrderId",referencedColumnName = "customer_order_id")
    private CustomerOrder customerOrder;

    @ManyToOne
    @JoinColumn(name="customerSupplyOrderId",referencedColumnName = "customer_supply_order_id")
    private CustomerSupplyOrder customerSupplyOrder;

    @Column(name = "expiration_date")
    private Date expirationDate;

    @Column(name = "its_no")
    private String itsNo;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "lot_no")
    private String lotNo;

    @Column(name = "position")
    private String position;

    @Column(name = "admition_date")
    private Date admitionDate;

    @Column(name = "sending_date")
    private Date sendingDate;

    @Column(name = "drug_barcode")
    private String drugBarcode;

    @Column(name = "note")
    private String note;

    @Column(name = "small_box_id")
    private Long smallBoxId;

    @Column(name = "box_id")
    private Long boxId;

    @OneToOne(mappedBy = "depot")
    private BoxDrugList boxDrugList;

    @Column(name = "stock_counting_explanation", nullable = true)
    private String stockCountingExplanation;

    @Column(name = "status", nullable = true)
    private Integer status;

    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "depot_status_id", referencedColumnName = "depot_status_id", nullable = false)
    private DepotStatus depotStatus;

    @ManyToOne
    @JoinColumn(name="refundOfferId",referencedColumnName = "refund_offer_id")
    private RefundOffer refundOffer;

    @ManyToOne
    @JoinColumn(name="refundId",referencedColumnName = "refund_id")
    private Refund refund;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "userid", referencedColumnName = "user_id", nullable = true)
    private User user;


}
