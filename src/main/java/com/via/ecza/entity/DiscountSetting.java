package com.via.ecza.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name="discount_setting")
public class DiscountSetting {

    @Id
    @SequenceGenerator(name = "sq_discount_setting", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_discount_setting")
    @Column(name = "discount_setting_id")
    private Long discountSettingId;

    // En küçük
    @Column(name = "price_0")
    private Double price0;

    @Column(name = "price_1")
    private Double price1;

    @Column(name = "price_2")
    private Double price2;

    @Column(name = "price_3")
    private Double price3;

    @Column(name = "price_4")
    private Double price4;

    // En Büyük
    @Column(name = "price_5")
    private Double price5;

    @Temporal(TemporalType.DATE)
    @Column(name = "created_date")
    private Date createdDate;
}
