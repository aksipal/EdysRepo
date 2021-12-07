package com.via.ecza.entity.enumClass;

public enum CheckingCardType {

    CUSTOMER("MÜŞTERİ"),
    SUPPLIER("TEDARİKÇİ"),
    OTHER("DİĞER");


    private String value;
    public String getValue() {
        return this.value;
    }
    CheckingCardType(String value) {
        this.value = value;
    }

}
