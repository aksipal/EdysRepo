package com.via.ecza.entity.enumClass;

public enum AccountType {

    EFT("EFT"),
    CHEQUE("ÇEK"),
    BOND("SENET"),
    CASH_BOX("KASA"),
    CREDIT_CARD("KREDİ KARTI"),
    BANK_CARD("BANKA KARTI"),;

    private String value;
    public String getValue() {
        return this.value;
    }
    AccountType(String value) {
        this.value = value;
    }
}
