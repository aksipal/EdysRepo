package com.via.ecza.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;


@Data
@Getter
@Setter
@Entity
@Table(name="payload_type")
public class PayloadType {

    @Id
    @SequenceGenerator(name = "sq_account_activity_id", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_account_activity_id")
    @Column(name = "payload_type_id")
    private Long payloadTypeId;

    @Column(name = "payload_type")
    private String payloadType;
}
