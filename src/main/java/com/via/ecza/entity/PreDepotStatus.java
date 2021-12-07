package com.via.ecza.entity;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Data
@Getter
@Setter
@Entity
@Table(name="pre_depot_status")
public class PreDepotStatus {

    @Id
    @SequenceGenerator(name = "sq_pre_depot_status", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_pre_depot_status")
    @Column (name= "pre_depot_status_id")
    private Long preDepotStatusId;

    private String statusName;
    private String explanation;
}
