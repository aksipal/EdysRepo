package com.via.ecza.dto;

import com.via.ecza.entity.enumClass.CameraType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Lob;

@Data
@Setter
@Getter
public class HandReaderOrderAcceptanceAcceptDto {
    private CameraType camera;
    @Lob
    private String qrcodeList;


    public HandReaderOrderAcceptanceAcceptDto(){

    }

}
