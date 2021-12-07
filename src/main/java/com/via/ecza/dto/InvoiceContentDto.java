package com.via.ecza.dto;

import com.via.ecza.entity.InvoicePurpose;
import com.via.ecza.entity.User;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Setter
@Getter
public class InvoiceContentDto {


    private Long utilityInvoiceContentId;

    // hizmet satış ortak alanlar
    private Double tagPrice;            // etiket fiyatı
    private Double wareHousemanPrice;   // depocu fiyatı
    private int generalDiscount;        // kurum iskonto
    private int sellDiscount;           // satış iskonto
    private int advanceDiscount;        // satış iskonto
    private Double netPrice;            // net fiyat
    private Double totalPrice;          // toplam fiyat //ödenecek tutar


    // hizmet satış ve alış ortak alanlar
    private String productService;      // Mal Hizmet
    private Double quantity;               // miktar
    private String unit;                // Birim
    private Double unitPrice;           // birim fiyatı
    private Double vat;                 // kdv
    private Double vatSum;              // kdv tutarı

    private CategoryDto category;
    // hizmet alış ortak alanlar
    private String productCode;         //Ürün Kodu
    private int discount;               //İskonto
    private Double discountSum;         //İskonto Tutarı
    private Double productServiceSum;   //Mal Hizmet Tutarı


    private int status;


    private Date createdAt;


    private InvoicePurpose invoicePurpose;


    //private Invoice invoice;

    private User user;


}
