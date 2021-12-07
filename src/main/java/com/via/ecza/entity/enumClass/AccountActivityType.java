package com.via.ecza.entity.enumClass;

public enum AccountActivityType {


    CUSTOMER_INVOICE("Müşteri Faturası"),   //charge
    CUSTOMER_PAYMENT("Müşteri Ödemesi"),    //debt
    PURCHASE_INVOICE("Alış Faturası"),      //debt
    PURCHASE_PAYMENT("Alış Ödemesi");       //charge


    private String value;
    public String getValue() {
        return this.value;
    }
    AccountActivityType(String value) {
        this.value = value;
    }
}
