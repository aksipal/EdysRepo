package com.via.ecza.entity;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Data
@Getter
@Setter
@Entity
@Table(name="depot_status")
public class DepotStatus {

    @Id
    @SequenceGenerator(name = "sq_depot_status", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_depot_status")
    @Column (name= "depot_status_id")
    private Long depotStatusId;

    private String statusName;
    private String explanation;
}
