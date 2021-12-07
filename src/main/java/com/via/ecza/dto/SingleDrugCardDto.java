package com.via.ecza.dto;

import com.sun.istack.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;



@Data
@Getter
@Setter
public class SingleDrugCardDto  {

    private Long id;

    @NotEmpty
    @NotNull
    private String drugName;

    @NotEmpty
    @NotNull
    private String drugDose;

    @NotEmpty
    @NotNull
    private String drugForm;

    @NotEmpty
    @NotNull
    private String drugPartiNo;

    @NotEmpty
    @NotNull
    private String category;
}
