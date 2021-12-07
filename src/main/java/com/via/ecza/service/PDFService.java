package com.via.ecza.service;


import com.itextpdf.text.Font;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.via.ecza.dto.CustomerSuppliersDto;
import com.via.ecza.dto.PharmacyOrdesDto;
import com.via.ecza.dto.PharmacyRefundDto;
import com.via.ecza.repo.UserRepository;
import javassist.NotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.*;
import java.util.*;
import java.util.List;

@Service
@Transactional
public class PDFService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ControlService controlService;
    @Autowired
    private ModelMapper mapper=new ModelMapper();

    public Boolean createPDF(List<Object> dto, String dtoName, String header, int columnSize, int[] columns, @NotNull @NotEmpty List<String> tableColumns, String pdfName) throws IOException, DocumentException, NotFoundException {

        //For money
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMANY);
        DecimalFormat df = (DecimalFormat)nf;
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);

        try {

            //PDF BAŞLANGIÇ
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("docs/" + pdfName + ".pdf"));
            document.open();

            Paragraph para = new Paragraph();

            Image image1 = Image.getInstance("docs/pharma.png");
            image1.setAlignment(Element.ALIGN_LEFT);
            image1.scaleAbsolute(60, 60);



            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            para.add(dateFormat.format(new Date()));
            para.setAlignment(Element.ALIGN_RIGHT);
            para.add(image1);
            document.add(para);




            BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
            Font catFont = new Font(bf, 16, Font.NORMAL, BaseColor.BLACK);

            //Tablonun Başlığı Girilir
            Paragraph tableHeader = new Paragraph(header, catFont);

            tableHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(tableHeader);

            document.add(new Paragraph("\n"));

            //Tablonun Sütun Sayısı Girilir
            PdfPTable table = new PdfPTable(columnSize);//columsize colums a eşit mi kontrol et
            table.setWidths(columns);

            table.setWidthPercentage(100);


            BaseFont bf1 = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
            Font catFont1 = new Font(bf1, 10, Font.NORMAL, BaseColor.BLACK);

            //Tablo Sütün Başlıkları Girilir
            tableColumns.forEach(columnTitle -> {
                PdfPCell header1 = new PdfPCell();
                header1.setBackgroundColor(BaseColor.LIGHT_GRAY);
                header1.setBorderWidth(1);
                header1.setPhrase(new Phrase(columnTitle, catFont1));
                header1.setPadding(3);


                table.addCell(header1);

            });

            //Hücrelere Veriler Girilir
            if (dtoName == "Satın Alma") {

                CustomerSuppliersDto[] cso1 = mapper.map(dto, CustomerSuppliersDto[].class);
                List<CustomerSuppliersDto> pdfDto = Arrays.asList(cso1);

                int a = 0;
                for (CustomerSuppliersDto pdf : pdfDto) {

                    a++;
                    addRows(table, String.valueOf(a));

                //    if (pdf.getDrugCard().getDrugName() != null) {
                        addRows(table, pdf.getDrugCard().getDrugName());
                 //   }

                 //   if (pdf.getSupplyOrderNo() != null) {
                        addRows(table, pdf.getSupplyOrderNo());
                 //   }

                 ///   if (pdf.getTotality() != null) {

                        addRows(table, pdf.getTotality().toString());
                 //   }

                 //   if (pdf.getStocks() != null) {
                        addRows(table, pdf.getStocks().toString());
                 //   }

                 //   if (pdf.getSupplier().getSupplierName() != null) {
                        addRows(table, pdf.getSupplier().getSupplierName());
                 //   }

                  //  if (pdf.getTotalPrice() != null) {
                        addRows(table, df.format(pdf.getTotalPrice())+"TL +KDV");
                  //  }
                  //  if (pdf.getCustomerSupplyStatus().getStatusName() != null) {

                        addRows(table, pdf.getCustomerSupplyStatus().getStatusName());
                  //  }

                }
            }

            if (dtoName == "Eczane Satış") {

                PharmacyOrdesDto[] cso1 = mapper.map(dto, PharmacyOrdesDto[].class);
                List<PharmacyOrdesDto> pdfDto = Arrays.asList(cso1);

                int a = 0;
                for (PharmacyOrdesDto pdf : pdfDto) {

                    a++;
                    addRows(table, String.valueOf(a));

                    //    if (pdf.getDrugCard().getDrugName() != null) {
                    String date = new SimpleDateFormat("dd-MM-yyyy").format(pdf.getCreatedAt());
                    addRows(table, date);
                    //   }

                    //   if (pdf.getSupplyOrderNo() != null) {
                     date = new SimpleDateFormat("dd-MM-yyyy").format(pdf.getCustomerOrderDrugs().getExpirationDate());
                    addRows(table,date);
                    //   }

                    ///   if (pdf.getTotality() != null) {

                    addRows(table, pdf.getDrugCard().getDrugName());
                    //   }

                    //   if (pdf.getStocks() != null) {
                    addRows(table, pdf.getQuantity().toString());
                    //   }

                    //   if (pdf.getSupplier().getSupplierName() != null) {
                    addRows(table, pdf.getSurplus());
                    //   }


                    addRows(table, pdf.getTotality().toString());
                    addRows(table, pdf.getSupplyOrderNo());

                    addRows(table, df.format(pdf.getTotalPrice())+"TL +KDV");
                    addRows(table, pdf.getCustomerSupplyStatus().getStatusName());


                }
            }
            if (dtoName == "Eczane İade") {

                PharmacyRefundDto[] cso1 = mapper.map(dto, PharmacyRefundDto[].class);
                List<PharmacyRefundDto> pdfDto = Arrays.asList(cso1);

                int a = 0;
                for (PharmacyRefundDto pdf : pdfDto) {

                    a++;
                    addRows(table, String.valueOf(a));


                    addRows(table, pdf.getRefundOrderNo());


                    String date = new SimpleDateFormat("dd-MM-yyyy").format(pdf.getCreatedAt());
                    addRows(table, date);


                    addRows(table, pdf.getDrugCard().getDrugName());


                    date = new SimpleDateFormat("dd-MM-yyyy").format(pdf.getExpirationDate());
                    addRows(table,date);


                    addRows(table, pdf.getTotality().toString());
                    //   }

                    //   if (pdf.getSupplier().getSupplierName() != null) {
                    addRows(table, df.format(pdf.getUnitPrice())+"TL +KDV");
                    //   }

                    addRows(table, df.format(pdf.getTotalPrice())+"TL +KDV");
                    addRows(table, pdf.getRefundStatus().getStatusName());


                }
            }

            document.add(table);
            document.close();

        } catch (Exception e) {
            throw new NotFoundException(" PDF Oluşturulamadı Hatalı veri girdiniz!");
        }


        return true;
    }

    private void addRows(PdfPTable table, String value) throws IOException, DocumentException {
        BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
        Font catFont = new Font(bf, 8, Font.NORMAL, BaseColor.BLACK);


        table.addCell(new Phrase(value, catFont));

    }


}
