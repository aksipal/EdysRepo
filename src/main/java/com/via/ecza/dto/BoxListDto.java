package com.via.ecza.dto;

import com.via.ecza.entity.Box;
import com.via.ecza.entity.User;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Data
@Getter
@Setter
public class BoxListDto {

    private Long boxId;
    private String boxCode;
    private String boxNo;
    private Double boxWeight;
    private int status;
    private int drugQuantity;
    private PackagingCustomerOrderDto customerOrder;
    private List<BoxDrugDetailListDto> boxDrugList;
//    private User user;
    private Double exactBoxWeight;

    public BoxListDto() {
    }

    public BoxListDto(Box box) {
        this.boxId = box.getBoxId();
        //this.boxCode = box.getBoxCode();
        this.boxNo = box.getBoxNo();
        this.boxWeight = box.getBoxWeight();
        this.status = box.getStatus();
        if(box.getCustomerOrder() != null) this.customerOrder = new PackagingCustomerOrderDto(box.getCustomerOrder());
//        this.user = box.getUser();

    }
}
