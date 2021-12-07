package com.via.ecza.dto;

import com.via.ecza.entity.BoxDrugList;
import com.via.ecza.entity.SmallBox;
import com.via.ecza.entity.User;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.OneToMany;
import java.util.List;

@Data
public class SmallBoxDto {

    private Long smallBoxId;
    private String smallBoxNo;
    private List<SingleBoxDrugListDto> boxDrugList;
    private Integer drugQuantity;
    private PackagingCustomerOrderDto customerOrder;
    //private User user;

    public SmallBoxDto() {

    }

    public SmallBoxDto(SmallBox smallBox) {

        this.smallBoxId = smallBox.getSmallBoxId();
        this.smallBoxNo = smallBox.getSmallBoxNo();
        this.drugQuantity = smallBox.getBoxDrugList().size();
        if(smallBox.getCustomerOrder() != null) this.customerOrder = new PackagingCustomerOrderDto(smallBox.getCustomerOrder());
        //this.user = smallBox.getUser();
    }
}
