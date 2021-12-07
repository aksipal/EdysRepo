package com.via.ecza.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

    import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
//@Getter
//@Setter
//@Entity
//@Table(name="pre_package")
public class PrePackage {


//    @Id
//    @SequenceGenerator(name = "sq_pre_package", initialValue = 1, allocationSize = 1)
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_pre_package")
//    @Column(name = "pre_package_id")
    private Long prePackageId;

//    @Type(type = "text")
//    @Lob
//    @Column(name = "drug_name",columnDefinition="text", length=10485760)
    private String drugName;

    //@Column(name = "drug_barcode")
    private Long drugBarcode;

    //@Column(name = "drug_serial_no")
    private String drugSerialNo;

    //@Column(name = "drug_expiration_date")
    //@NotNull
    private Date drugExpirationDate;

    //@Column(name = "drug_lot_no")
    private String drugLotNo;

    //@Column(name = "drug_its_no")
    private String drugItsNo;

    //@ManyToOne
    //@JoinColumn(name="customerOrderId",referencedColumnName = "customer_order_id", nullable = true)
    private CustomerOrder customerOrder;

    //@ManyToOne
    //@JoinColumn(name="customerSupplyOrderId",referencedColumnName = "customer_supply_order_id", nullable = true)
    private CustomerSupplyOrder customerSupplyOrder;

}