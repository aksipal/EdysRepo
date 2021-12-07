package com.via.ecza.dto;

import com.via.ecza.entity.DrugCard;
import com.via.ecza.entity.User;
import lombok.Data;
import java.util.Date;

@Data
public class PreCustomerOrderDrugsListDto {
    private Long preCustomerOrderDrugId;
    private Long totalQuantity;
    private Date expirationDate;
    private String customerOrderDrugNote;
    private DrugCard drugCard;
    private User user;
}
