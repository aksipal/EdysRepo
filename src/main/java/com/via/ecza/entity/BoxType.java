package com.via.ecza.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name="box_type")
public class BoxType {

    @Id
    @SequenceGenerator(name = "sq_box_type", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_box_type")
    @Column(name = "box_type_id")
    private Long boxTypeId;

    @Column(name = "box_type")
    private String boxType;

}
