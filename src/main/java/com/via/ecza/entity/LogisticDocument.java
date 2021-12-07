package com.via.ecza.entity;

import com.via.ecza.entity.enumClass.LogisticDocumentType;
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
@Table(name="logistic_document")
public class LogisticDocument {

    @Id
    @SequenceGenerator(name = "sq_logistic_document", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_logistic_document")
    @Column(name = "logistic_document_id")
    private Long logisticDocumentId;

    @ManyToOne
    @JoinColumn(name = "customer_order_id", referencedColumnName = "customer_order_id", nullable = false)
    private CustomerOrder customerOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type")
    private LogisticDocumentType logisticDocumentType;

    @Lob
    @Type(type = "text")
    @Column(name = "document_name")
    private String documentName;

    @Column(name = "created_at")
    private Date createdAt;

    private int status;




}
