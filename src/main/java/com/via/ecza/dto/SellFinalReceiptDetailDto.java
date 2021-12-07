package com.via.ecza.dto;


import lombok.Data;

import java.util.List;

@Data
public class SellFinalReceiptDetailDto {

    private Long supplierId;
    private List<ReceiptRefundListDto> refundList;
    private List<CustomerOrderDrugsListDto> customerOrderDrugsList;
}
