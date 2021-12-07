package com.via.ecza.entity.enumClass;

public enum BoxSize {

    SIZE_40("40*40*40"),
    SIZE_50("50*50*50"),
    SIZE_60("60*60*60");

    private String value;
    public String getValue() {
        return this.value;
    }
    BoxSize(String value) {
        this.value = value;
    }
}
