package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class QrCodeDto {

    private Long qrCodeId;
    private String qrCode;
    private int status;

    public QrCodeDto() {
    }
}
