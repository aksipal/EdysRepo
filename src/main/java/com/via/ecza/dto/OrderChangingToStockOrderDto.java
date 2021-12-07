package com.via.ecza.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class OrderChangingToStockOrderDto {


    private Long customerSupplyOrderId;
    private Long customerOrderId;


    public OrderChangingToStockOrderDto() { }

}
