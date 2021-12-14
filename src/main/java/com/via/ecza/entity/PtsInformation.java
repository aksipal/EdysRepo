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
@Table(name="pts_information")
public class PtsInformation {
    @Id
    @SequenceGenerator(name = "sq_pts_information", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_pts_information")
    @Column(name = "pts_information_id")
    private Long ptsInformationId;

    @Column(name = "box_barcode")
    private String boxBarcode;

    @Column(name = "drug_qr_code")
    private String drugQrCode;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "status", nullable = true)
    private Integer status;

}
