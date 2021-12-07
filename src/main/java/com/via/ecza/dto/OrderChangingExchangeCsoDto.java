package com.via.ecza.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class OrderChangingExchangeCsoDto {

    private Long csoFalseId;
    private Long csoTrueId;
}
