package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Lob;

@Getter
@Setter
@Data
public class QrCodeSaveDto {
    @Lob
    private String qrcode;
}
