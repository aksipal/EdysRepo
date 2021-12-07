package com.via.ecza.dto;


import com.via.ecza.entity.*;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
public class ReceiptInvoiceSearchDto {

    private Long receiptId;

    private String receiptNo;

    private String pharmacyReceiptNo;

    private Long supplierId;

    private Date startDate;

    private Date endDate;

    private Date dueDate;

    private String dispatchNo;

    private Date dispatchDate;

}
