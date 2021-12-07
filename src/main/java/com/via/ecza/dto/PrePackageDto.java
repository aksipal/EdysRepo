package com.via.ecza.dto;

import com.via.ecza.entity.PreDepotStatus;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Getter
@Setter
@Data
public class PrePackageDto {

    private Long prePackageId;
    @Lob
    private String drugName;
    private Long drugBarcode;
    private Long drugSerialNo;
    private Date drugExpirationDate;
    private String drugLotNo;
    private String drugItsNo;
    private PackagingCustomerOrderDto customerOrder;
    private DepotCustomerSupplierOrderListDto customerSupplyOrder;

    public PrePackageDto(){

    }

}
