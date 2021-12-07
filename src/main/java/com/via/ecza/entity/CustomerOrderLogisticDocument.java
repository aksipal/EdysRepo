package com.via.ecza.entity;

import com.via.ecza.entity.enumClass.LogisticDocumentType;
import com.via.ecza.entity.enumClass.LogisticFileType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Data
@Getter
@Setter
@Entity
@ToString
@Table(name = "customer_order_logistic_document")
public class CustomerOrderLogisticDocument {
    @Id
    @SequenceGenerator(name = "sq_customer_order_logistic_document", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_customer_order_logistic_document")
    @Column(name = "customer_order_logistic_document_id")
    private Long customerOrderLogisticDocumentId;

    @ManyToOne
    @JoinColumn(name = "customer_order_id", referencedColumnName = "customer_order_id", nullable = true)
    private CustomerOrder customerOrder;

    @Column(name = "file_type")
    private LogisticFileType fileType;

    @Column(name = "document_type")
    private LogisticDocumentType documentType;

    @Column(name = "file_name")
    private String fileName;

}
