package com.via.ecza.service;


import com.via.ecza.dto.CustomerSuppliersDto;
import com.via.ecza.dto.DepotDto;
import com.via.ecza.dto.PharmacyOrdesDto;
import com.via.ecza.dto.PharmacyRefundDto;
import com.via.ecza.entity.Depot;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
@Transactional
public class ExcelService {

 @Autowired
 private EntityManager entityManager;
 @Autowired
 private ModelMapper mapper= new ModelMapper();


    public Boolean createExcel(List<Object> dto,String dtoName, String header, Integer rowHeadsCount,int[] columnsSize, List<String> tableColumns,String excelName ) throws IOException {

        //For money
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMANY);
        DecimalFormat df = (DecimalFormat)nf;
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);



        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Sayfa1");


        //STYLE BAŞLANGIÇ
        CellStyle csBody = workbook.createCellStyle();
        XSSFFont fontBody = workbook.createFont();
        fontBody.setFontName("Times New Roman");
        fontBody.setFontHeightInPoints((short) 11);
        csBody.setWrapText(true);
        csBody.setLocked(false);
        csBody.setFont(fontBody);
        csBody.setAlignment(HorizontalAlignment.CENTER);
        csBody.setBorderBottom(BorderStyle.MEDIUM);
        csBody.setBorderTop(BorderStyle.MEDIUM);
        csBody.setBorderLeft(BorderStyle.MEDIUM);
        csBody.setBorderRight(BorderStyle.MEDIUM);

        //STYLE SON

        addExcelHeader(workbook,sheet,  header,  rowHeadsCount, columnsSize,  tableColumns );
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        if (dtoName == "Satın Alma") {

            CustomerSuppliersDto[] cso1 = mapper.map(dto, CustomerSuppliersDto[].class);
            List<CustomerSuppliersDto> pdfDto = Arrays.asList(cso1);

            int a = 6;
            int b = 0;
            for (CustomerSuppliersDto csd : pdfDto) {
                a++;
                b++;
                XSSFRow row = sheet.createRow((short) a);
                row.createCell(0).setCellValue(b);
                row.getCell(0).setCellStyle(csBody);

                //İçerik Kısmı Bu Satırdan İtibaren Obje Bilgileri İle Doldurulur
                row.createCell(1).setCellValue(csd.getDrugCard().getDrugName());
                row.getCell(1).setCellStyle(csBody);
                row.createCell(2).setCellValue( df.format(csd.getAverageUnitPrice())+"TL +KDV");
                row.getCell(2).setCellStyle(csBody);
                row.createCell(3).setCellValue(csd.getQuantity());
                row.getCell(3).setCellStyle(csBody);
                row.createCell(4).setCellValue(csd.getSurplus());
                row.getCell(4).setCellStyle(csBody);
                row.createCell(5).setCellValue(csd.getTotality());
                row.getCell(5).setCellStyle(csBody);
                row.createCell(6).setCellValue( df.format(csd.getTotalPrice())+"TL +KDV");
                row.getCell(6).setCellStyle(csBody);
                row.createCell(7).setCellValue(csd.getCustomerSupplyStatus().getStatusName());
                row.getCell(7).setCellStyle(csBody);



        }}

        if(dtoName.equals("Depot")){

            DepotDto[] dtos = mapper.map(dto, DepotDto[].class);
            List<DepotDto> dtosList = Arrays.asList(dtos);

        int a = 6;
        int b = 0;
        for (DepotDto depot : dtosList) {
            a++;
            b++;
            XSSFRow row = sheet.createRow((short) a);
            row.createCell(0).setCellValue(b);
            row.getCell(0).setCellStyle(csBody);

            //İçerik Kısmı Bu Satırdan İtibaren Obje Bilgileri İle Doldurulur
            row.createCell(1).setCellValue(depot.getDrugCard().getDrugName());
            row.getCell(1).setCellStyle(csBody);
            row.createCell(2).setCellValue(depot.getDrugBarcode());
            row.getCell(2).setCellStyle(csBody);
            row.createCell(3).setCellValue(depot.getSerialNumber());
            row.getCell(3).setCellStyle(csBody);
            row.createCell(4).setCellValue(depot.getLotNo());
            row.getCell(4).setCellStyle(csBody);
            row.createCell(5).setCellValue(dateFormat.format(depot.getExpirationDate()));
            row.getCell(5).setCellStyle(csBody);
            row.createCell(6).setCellValue(depot.getPosition());
            row.getCell(6).setCellStyle(csBody);
            row.createCell(7).setCellValue(depot.getDepotStatus().getExplanation());
            row.getCell(7).setCellStyle(csBody);
            row.createCell(8).setCellValue(depot.getCustomerOrder().getCustomerOrderNo());
            row.getCell(8).setCellStyle(csBody);
            row.createCell(9).setCellValue(depot.getCustomerSupplyOrder().getSupplyOrderNo());
            row.getCell(9).setCellStyle(csBody);

        }}
        if(dtoName.equals("Eczane Satış")){

            PharmacyOrdesDto[] cso1 = mapper.map(dto, PharmacyOrdesDto[].class);
            List<PharmacyOrdesDto> pdfDto = Arrays.asList(cso1);

            int a = 6;
            int b = 0;
            for (PharmacyOrdesDto csd : pdfDto) {
                a++;
                b++;
                XSSFRow row = sheet.createRow((short) a);
                row.createCell(0).setCellValue(b);
                row.getCell(0).setCellStyle(csBody);


                String date = new SimpleDateFormat("dd-MM-yyyy").format(csd.getCreatedAt());
                row.createCell(1).setCellValue(date);
                row.getCell(1).setCellStyle(csBody);

                 date = new SimpleDateFormat("dd-MM-yyyy").format(csd.getCustomerOrderDrugs().getExpirationDate());
                row.createCell(2).setCellValue( date);
                row.getCell(2).setCellStyle(csBody);


                row.createCell(3).setCellValue(csd.getDrugCard().getDrugName());
                row.getCell(3).setCellStyle(csBody);


                row.createCell(4).setCellValue(csd.getQuantity().toString());
                row.getCell(4).setCellStyle(csBody);


                row.createCell(5).setCellValue(csd.getSurplus());
                row.getCell(5).setCellStyle(csBody);


                row.createCell(6).setCellValue( csd.getTotality().toString());
                row.getCell(6).setCellStyle(csBody);


                row.createCell(7).setCellValue(csd.getSupplyOrderNo());
                row.getCell(7).setCellStyle(csBody);

                row.createCell(8).setCellValue(df.format(csd.getTotalPrice())+"TL +KDV");
                row.getCell(8).setCellStyle(csBody);

                row.createCell(9).setCellValue(csd.getCustomerSupplyStatus().getStatusName());
                row.getCell(9).setCellStyle(csBody);


            }}


        if(dtoName.equals("Eczane İade")){

            PharmacyRefundDto[] cso1 = mapper.map(dto, PharmacyRefundDto[].class);
            List<PharmacyRefundDto> pdfDto = Arrays.asList(cso1);

            int a = 6;
            int b = 0;
            for (PharmacyRefundDto csd : pdfDto) {
                a++;
                b++;
                XSSFRow row = sheet.createRow((short) a);
                row.createCell(0).setCellValue(b);
                row.getCell(0).setCellStyle(csBody);


                row.createCell(1).setCellValue(csd.getRefundOrderNo());
                row.getCell(1).setCellStyle(csBody);

                String date = new SimpleDateFormat("dd-MM-yyyy").format(csd.getCreatedAt());
                row.createCell(2).setCellValue( date);
                row.getCell(2).setCellStyle(csBody);


                row.createCell(3).setCellValue(csd.getDrugCard().getDrugName());
                row.getCell(3).setCellStyle(csBody);


                 date = new SimpleDateFormat("dd-MM-yyyy").format(csd.getExpirationDate());
                row.createCell(4).setCellValue(date);
                row.getCell(4).setCellStyle(csBody);


                row.createCell(5).setCellValue(csd.getTotality());
                row.getCell(5).setCellStyle(csBody);


                row.createCell(6).setCellValue( df.format(csd.getUnitPrice())+"TL +KDV");
                row.getCell(6).setCellStyle(csBody);


                row.createCell(7).setCellValue(df.format(csd.getTotalPrice())+"TL +KDV");
                row.getCell(7).setCellStyle(csBody);

                row.createCell(8).setCellValue(csd.getRefundStatus().getStatusName());
                row.getCell(8).setCellStyle(csBody);


            }}


        FileOutputStream fileOut = new FileOutputStream("docs/"+excelName+".xlsx");
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();



return true;


    }


    private void addExcelHeader(XSSFWorkbook workbook, XSSFSheet sheet, String header, Integer rowHeadsCount,int[] columnsSize, List<String> tableColumns ) throws IOException {
        //STYLE BAŞLANGIÇ
        XSSFFont fontHeader = workbook.createFont();
        fontHeader.setFontHeightInPoints((short) 12);
        fontHeader.setFontName("Times New Roman");
        CellStyle csHeader = workbook.createCellStyle();
        csHeader.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
        csHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        csHeader.setWrapText(true);
        csHeader.setLocked(false);
        csHeader.setAlignment(HorizontalAlignment.CENTER);
        csHeader.setFont(fontHeader);

        CellStyle csHeading = workbook.createCellStyle();
        XSSFFont fontHeading = workbook.createFont();
        fontHeading.setFontName("Times New Roman");
        fontHeading.setFontHeightInPoints((short) 14);
        csHeading.setFont(fontHeading);
        csHeading.setLocked(false);
        csHeading.setAlignment(HorizontalAlignment.CENTER);

        CellStyle csDate = workbook.createCellStyle();
        XSSFFont fontDate = workbook.createFont();
        fontDate.setFontName("Times New Roman");
        fontDate.setFontHeightInPoints((short) 12);
        csDate.setFont(fontDate);
        csDate.setAlignment(HorizontalAlignment.RIGHT);
        csDate.setLocked(false);
        //STYLE SON

        XSSFRow rowDate = sheet.createRow((short) 2);
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        //Bu kısım Kolon Sayısına Eşit Olmalı
        rowDate.createCell(rowHeadsCount-1).setCellValue(dateFormat.format(new Date()));
        rowDate.getCell(rowHeadsCount-1).setCellStyle(csDate);


        InputStream inputStream = new FileInputStream("docs/pharma.png");
        byte[] bytes = IOUtils.toByteArray(inputStream);
        int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
        inputStream.close();
        CreationHelper helper = workbook.getCreationHelper();
        Drawing drawing = sheet.createDrawingPatriarch();
        ClientAnchor anchor = helper.createClientAnchor();
        anchor.setCol1(0);
        anchor.setRow1(0);
        Picture pict = drawing.createPicture(anchor, pictureIdx);
        pict.resize(1.8, 3.6);


        //Başlık verilir
        XSSFRow rowHeader = sheet.createRow((short) 4);
        rowHeader.createCell(0).setCellValue(header);
        rowHeader.getCell(0).setCellStyle(csHeading);

        sheet.addMergedRegion(new CellRangeAddress(0, 2, 0, 1));
        //son parametre kolon sayısına eşit olmalı
        sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, rowHeadsCount-1));


        XSSFRow rowhead = sheet.createRow((short) 6);

        //Kolon İsimleri Belirtilir İhtiyaç Olursa Kolonlar Genişletilir

        for(int i=0;i<rowHeadsCount;i++){


            rowhead.createCell(i).setCellValue(tableColumns.get(i));
            rowhead.getCell(i).setCellStyle(csHeader);
            sheet.setColumnWidth(i, columnsSize[i]);

        }



        //A4 Sayfaya Sığdırma
        sheet.setFitToPage(true);
        PrintSetup ps = sheet.getPrintSetup();
        ps.setFitWidth( (short) 1.5);
        ps.setFitHeight( (short) 0);


    }

}
