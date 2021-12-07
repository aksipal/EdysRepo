package com.via.ecza.entity;

import com.sun.istack.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

@Data
@Getter
@Setter
@Entity
@Table(name="barcode")
public class Barcode {
    @Id
    @SequenceGenerator(name = "sq_barcode", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_barcode")
    @Column(name = "barcode_id")
    private Long barcodeId;

    @Column(name = "barcode")
    @NotEmpty
    @NotNull
    private String barcode;

    private int status;

    @Column(name="camera_type", nullable = true)
    private int cameraType;
}
