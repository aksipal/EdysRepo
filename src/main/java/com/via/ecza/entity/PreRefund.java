package com.via.ecza.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@Getter
@Setter
@Entity
@Table(name="pre_refund")
public class PreRefund {


    @Id
    @SequenceGenerator(name = "sq_pre_refund", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_pre_refund")
    @Column(name = "pre_refund_id")
    private Long preRefundId;

    @Type(type = "text")
    @Lob
    @Column(name = "drug_name",columnDefinition="text", length=10485760)
    private String drugName;

    @Column(name = "drug_barcode")
    private Long drugBarcode;

    @Column(name = "drug_serial_no")
    private String drugSerialNo;

    @Column(name = "drug_expiration_date")
    @NotNull
    private Date drugExpirationDate;

    @Column(name = "drug_lot_no")
    private String drugLotNo;

    @Column(name = "drug_its_no")
    private String drugItsNo;

    @ManyToOne
    @JoinColumn(name="refundId",referencedColumnName = "refund_id")
    private Refund refund;

    @Column(name = "admition_date")
    private Date admitionDate;

    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "preRefundStatusId", referencedColumnName = "pre_refund_status_id", nullable = false)
    private PreRefundStatus PreRefundStatus;


}