package com.via.ecza.entity.enumClass;

public enum Role {
    //USER("ÜYE"),
    ADMIN("ADMIN"),
    //CUSTOMER("MÜŞTERİ"),
//    SUPPLIER("TEDARİKÇİ"),
    EXPORTER("İHRACATÇI"),
    MANAGER("MÜDÜR"),
    PHARMACY("ECZACI"),
    PURCHASE("SATIN ALMA"),
    WAREHOUSEMAN ("DEPOCU"),
    ACCOUNTING ("MUHASEBECİ"),
    LOGISTIC ("LOJİSTİKÇİ");


    private String value;
    public String getValue() {
        return this.value;
    }
    Role(String value) {
        this.value = value;
    }

}
