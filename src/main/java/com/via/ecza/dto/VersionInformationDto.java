package com.via.ecza.dto;

import lombok.Data;

import javax.persistence.Lob;
import java.util.Date;

@Data
public class VersionInformationDto {

    private Long versionInformationId;
    private String versionNumber;
    @Lob
    private String versionClauses ;
    private Date createdAt;

}
