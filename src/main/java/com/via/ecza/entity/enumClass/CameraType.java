package com.via.ecza.entity.enumClass;

public enum CameraType {

    CAMERA_1("KAMERA 1"),
    CAMERA_2("KAMERA 2");


    private String value;
    public String getValue() {
        return this.value;
    }
    CameraType(String value) {
        this.value = value;
    }
}
