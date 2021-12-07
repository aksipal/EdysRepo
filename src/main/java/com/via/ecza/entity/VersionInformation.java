package com.via.ecza.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;

@Data
@Getter
@Setter
@Entity
@Table(name="version_information")
public class VersionInformation {

    @Id
    @SequenceGenerator(name = "sq_version_information", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_version_information")
    @Column(name = "version_information_id")
    private Long versionInformationId;

    @Column(name = "version_number")
    private String versionNumber;

    @Type(type = "text")
    @Lob
    @Column(name = "version_clauses ",columnDefinition="text", length=10485760)
    private String versionClauses ;

    @Column(name = "created_at")
    private Date createdAt;
}