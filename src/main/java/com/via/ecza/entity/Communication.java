package com.via.ecza.entity;

import com.via.ecza.entity.enumClass.CameraType;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name="communication")
public class Communication {
    @Id
    @SequenceGenerator(name = "sq_communication", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_communication")
    @Column(name = "communication_id")
    private Long communicationId;

    @Column(name = "barcode")
    private Long barcode;

    @Column(name = "total_quantity")
    private Long totalQuantity;

    @Column(name = "expiration_date")
    private Date expirationDate;

//    @Enumerated(EnumType.ORDINAL)
//    @Column(name="camera_type", nullable = false)
    @Transient
    private CameraType camera;

    @Column(name = "status")
    private int status;

    @Column(name="camera_type", nullable = false)
    private int cameraType;

}
