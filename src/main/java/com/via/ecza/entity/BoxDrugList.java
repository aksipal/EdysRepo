package com.via.ecza.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Table(name="box_drug_list")
public class BoxDrugList {
    @Id
    @SequenceGenerator(name = "sq_box_drug_list", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_box_drug_list")
    @Column(name = "box_drug_list_id")
    private Long boxDrugListId;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.EAGER)
    @JoinColumn(name="depot_id",referencedColumnName = "depot_id")
    private Depot depot;

    @ManyToOne
    @JoinColumn(name="small_box_id", referencedColumnName = "small_box_id", nullable = true)
    private SmallBox smallBox;

    @ManyToOne
    @JoinColumn(name="box_id", referencedColumnName = "box_id", nullable = true)
    private Box box;

}
