package com.via.ecza.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class SupervisorDto {
    private Long supplierSupervisorId;
    private String name;
    private String surname;
    private String email;
    private String phoneNumber;
    private String jobTitle;
    private int status;
}
