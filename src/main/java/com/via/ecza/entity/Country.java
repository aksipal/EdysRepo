package com.via.ecza.entity;

import java.util.Date;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

import com.sun.istack.NotNull;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Entity
@Table(name="country")
public class Country {

    @Id
    @SequenceGenerator(name = "sq_country", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_country")
    @Column(name = "country_id")
    private Long countryId;

    @Column(name = "name")
    @NotEmpty
    @NotNull
    private String name;

    @Column(name = "english_name")
    @NotEmpty
    @NotNull
    private String englishName;

}
