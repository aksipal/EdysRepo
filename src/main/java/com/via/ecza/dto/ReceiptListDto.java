package com.via.ecza.dto;

import com.via.ecza.entity.ReceiptStatus;
import com.via.ecza.entity.Refund;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import java.util.Date;
import java.util.List;

@Data
@Getter
@Setter
public class ReceiptListDto {
    private Long receiptId;
    private String receiptType;
    private Long receiptStatusId;
    private ReceiptStatus receiptStatus;
    private Long supplierId;
    private Date createdAt;
    private ReceiptInvoiceDto invoice;
    private String invoiceNo;
    private Date invoiceDate;
    private String receiptNote;
    private ReceiptSupplierDto supplier;
    private String receiptNo;
    private Long totality;
    private Double totalPrice;
    private Double totalPriceWithVat;
    private List<ReceiptRefundDto> refunds;

}
