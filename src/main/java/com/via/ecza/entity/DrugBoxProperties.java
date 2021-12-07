package com.via.ecza.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name="drug_box_properties")
public class DrugBoxProperties {

    @Id
    @SequenceGenerator(name = "sq_drug_box_properties", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_drug_box_properties")
    @Column(name = "drug_box_properties_id")
    private Long drugBoxPropertiesId;

    //Kutu Ağırlığı
    @Column(name = "drug_box_weight")
    private Double drugBoxWeight;

    //Kutu Eni
    @Column(name = "drug_box_width")
    private Double drugBoxWidth;

    //Kutu Boyu
    @Column(name = "drug_box_length")
    private Double drugBoxLength;

    //Kutu Yüksekliği
    @Column(name = "drug_box_height")
    private Double drugBoxHeight;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "drug_card_id", referencedColumnName = "drug_card_id", nullable = false)
    private DrugCard drugCard;

}
