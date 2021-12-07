package com.via.ecza.dto;

import com.via.ecza.entity.CustomerOrder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CustomerOrderBankDetailSaveDto {
    private Long customerOrderBankDetailId;
    private Long customerOrderId;
    private String accountName;
    private String bankName;
    private String ibanNo;
    private String swift;



    public CustomerOrderBankDetailSaveDto(){

    }
}
