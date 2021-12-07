package com.via.ecza.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class Currency {
    private Integer crossOrder;
    private Integer unit;
    private Integer crossRateUSD;
    private String kod;
    private String currencyCode;
    private String isim;
    private String currencyName;
    private Double forexBuying;
    private Double forexSelling;
    private Double banknoteBuying;
    private Double banknoteSelling;
}
