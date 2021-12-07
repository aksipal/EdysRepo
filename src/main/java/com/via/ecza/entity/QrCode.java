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
@Table(name="qr_code")
public class QrCode {
    @Id
    @SequenceGenerator(name = "sq_qr_code", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_qr_code")
    @Column(name = "qr_code_id")
    private Long qrCodeId;

    @Column(name = "qr_code")
    @NotEmpty
    @NotNull
    private String qrCode;

    @Column(name = "status")
    private int status;

    @Column(name="camera_type", nullable = false)
    private int cameraType;
}
