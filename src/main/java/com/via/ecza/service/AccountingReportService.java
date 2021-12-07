package com.via.ecza.service;


import com.via.ecza.dto.AccountingReportSearchDto;
import com.via.ecza.dto.CustomerOrderDrugsExcelDto;
import com.via.ecza.dto.CustomerSupplyOrderExcelDto;
import com.via.ecza.entity.CustomerOrderDrugs;
import com.via.ecza.entity.CustomerSupplyOrder;
import com.via.ecza.entity.User;
import com.via.ecza.repo.CustomerOrderDrugsRepository;
import com.via.ecza.repo.CustomerOrderRepository;
import javassist.NotFoundException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class AccountingReportService {
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private CustomerOrderDrugsRepository customerOrderDrugsRepository;
    @Autowired
    private CustomerOrderRepository customerOrderRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ControlService controlService;


    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");


    public Page<CustomerOrderDrugsExcelDto> searchReportSellExcel(String authHeader, Pageable page, AccountingReportSearchDto dto) {
        StringBuilder createSqlQuery ;
        createSqlQuery = new StringBuilder("select * from customer_order_drugs cod  ");
        createSqlQuery.append(" join customer_order co ON co.customer_order_id = cod.customer_order_id ");
        createSqlQuery.append(" join customer_receipt cr on cr.customer_receipt_id = cod.customer_receipt_id ");
        createSqlQuery.append(" join final_receipt fr on fr.final_receipt_id = cr.final_receipt_id ");
        createSqlQuery.append(" join invoice i on i.invoice_id = fr.invoice_id ");
        createSqlQuery.append(" where co.status = 1 and co.order_status_id >= 30 and i.invoice_status_id >= 10 ");

        if (dto.getStartDate() != null )
            createSqlQuery.append(" and i.created_at  >= to_date('" + sdf.format(dto.getStartDate()) + "'," + "'dd.MM.yyyy') ");
        if (dto.getEndDate() != null )
            createSqlQuery.append(" and i.created_at  < to_date('" + sdf.format(dto.getEndDate()) + "'," + "'dd.MM.yyyy') ");
        if(dto.getCustomerOrderNo() != null)
            if (dto.getCustomerOrderNo().trim().length() > 0)
                createSqlQuery.append(" and co.customer_order_no = '" +dto.getCustomerOrderNo().trim() +"'");
        createSqlQuery.append(" order by cod.customer_order_drug_id");

        List<CustomerOrderDrugs> list = entityManager.createNativeQuery(createSqlQuery.toString(), CustomerOrderDrugs.class).getResultList();
        CustomerOrderDrugsExcelDto[] dtos = mapper.map(list, CustomerOrderDrugsExcelDto[].class);
        List<CustomerOrderDrugsExcelDto> dtosList = Arrays.asList(dtos);

        int start = Math.min((int) page.getOffset(), dtosList.size());
        int end = Math.min((start + page.getPageSize()), dtosList.size());

        Page<CustomerOrderDrugsExcelDto> pageList = new PageImpl<>(dtosList.subList(start, end), page, dtosList.size());

        return pageList;
    }

    public String createReportSellExcel(String authHeader, AccountingReportSearchDto dto) throws Exception {
        User user = controlService.getUserFromToken(authHeader);
        StringBuilder createSqlQuery ;
        createSqlQuery = new StringBuilder("select * from customer_order_drugs cod  ");
        createSqlQuery.append(" join customer_order co ON co.customer_order_id = cod.customer_order_id ");
        createSqlQuery.append(" join customer_receipt cr on cr.customer_receipt_id = cod.customer_receipt_id ");
        createSqlQuery.append(" join final_receipt fr on fr.final_receipt_id = cr.final_receipt_id ");
        createSqlQuery.append(" join invoice i on i.invoice_id = fr.invoice_id ");
        createSqlQuery.append(" where co.status = 1 and co.order_status_id >= 30 and i.invoice_status_id >= 10 ");

        if (dto.getStartDate() != null )
            createSqlQuery.append(" and i.created_at  >= to_date('" + sdf.format(dto.getStartDate()) + "'," + "'dd.MM.yyyy') ");
        if (dto.getEndDate() != null )
            createSqlQuery.append(" and i.created_at  < to_date('" + sdf.format(dto.getEndDate()) + "'," + "'dd.MM.yyyy') ");
        if(dto.getCustomerOrderNo() != null)
            if (dto.getCustomerOrderNo().trim().length() > 0)
                createSqlQuery.append(" and co.customer_order_no = '" +dto.getCustomerOrderNo().trim() +"'");
        createSqlQuery.append(" order by co.customer_order_no, cod.customer_order_drug_id");

        List<CustomerOrderDrugs> list = entityManager.createNativeQuery(createSqlQuery.toString(), CustomerOrderDrugs.class).getResultList();

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

        addCustomerOrderExcelHeader(workbook,sheet);

        int a = 4;
        int b = 0;
        for (CustomerOrderDrugs data : list) {
            a++;
            b++;
            XSSFRow row = sheet.createRow((short) a);
            row.createCell(0).setCellValue(b);
            row.getCell(0).setCellStyle(csBody);

            row.createCell(1).setCellValue(data.getCustomerOrder().getCustomerOrderNo());
            row.getCell(1).setCellStyle(csBody);

            row.createCell(2).setCellValue(sdf.format(data.getCustomerReceipt().getInvoice().getCreatedAt()));
            row.getCell(2).setCellStyle(csBody);

            if(data.getCustomerOrder().getCustomer().getCompany() != null){
                row.createCell(3).setCellValue(data.getCustomerOrder().getCustomer().getCompany().getCompanyName());
                row.getCell(3).setCellStyle(csBody);
            }
            else{
                row.createCell(3).setCellValue(data.getCustomerOrder().getCustomer().getName()+ " "+ data.getCustomerOrder().getCustomer().getSurname());
                row.getCell(3).setCellStyle(csBody);
            }
            row.createCell(4).setCellValue(data.getDrugCard().getDrugName());
            row.getCell(4).setCellStyle(csBody);

            row.createCell(5).setCellValue(data.getTotalQuantity());
            row.getCell(5).setCellStyle(csBody);
        }

        String excelTitle="activity-excel_"+user.getUserId();

        FileOutputStream fileOut = new FileOutputStream("docs/"+excelTitle+".xlsx");
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();

        return excelTitle;
    }

    private void addCustomerOrderExcelHeader(XSSFWorkbook workbook, XSSFSheet sheet) throws IOException {

        CellStyle csHeader = this.createCSHeaderForExcelHeader(workbook);
        //CellStyle csBody = this.createCSBodyForExcelHeader(workbook);
        CellStyle csTableTitle = this.createCSTableTitleForExcelHeader(workbook);

        //Başlık verilir
        XSSFRow rowHeader = sheet.createRow((short) 0);
        rowHeader.createCell(0).setCellValue("RAPOR : ILAC SATIS RAPORU (DETAYLI)");
        rowHeader.getCell(0).setCellStyle(csHeader);


        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 4));
        XSSFRow rowDate = sheet.createRow((short) 2);

        //Bu kısım Kolon Sayısına Eşit Olmalı
        rowDate.createCell(0).setCellValue(sdf.format(new Date()));
        rowDate.getCell(0).setCellStyle(csHeader);

//
//        InputStream inputStream = new FileInputStream("docs/pharma.png");
//        byte[] bytes = IOUtils.toByteArray(inputStream);
//        int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
//        inputStream.close();
//        CreationHelper helper = workbook.getCreationHelper();
//        Drawing drawing = sheet.createDrawingPatriarch();
//        ClientAnchor anchor = helper.createClientAnchor();
//        anchor.setCol1(0);
//        anchor.setRow1(0);
//        Picture pict = drawing.createPicture(anchor, pictureIdx);
//        pict.resize(1.8, 3.6);




//        sheet.addMergedRegion(new CellRangeAddress(0, 2, 0, 1));
//        //son parametre kolon sayısına eşit olmalı
//        sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 10));
//

        XSSFRow rowhead = sheet.createRow((short) 4);

        rowhead.createCell(0).setCellValue("No");
        rowhead.getCell(0).setCellStyle(csTableTitle);
        sheet.setColumnWidth(0, 1500);
        rowhead.createCell(1).setCellValue("Sipariş No");
        rowhead.getCell(1).setCellStyle(csTableTitle);
        sheet.setColumnWidth(1, 5000);
        rowhead.createCell(2).setCellValue("Tarih");
        rowhead.getCell(2).setCellStyle(csTableTitle);
        sheet.setColumnWidth(2, 3500);
        rowhead.createCell(3).setCellValue("Hesap İsmi");
        rowhead.getCell(3).setCellStyle(csTableTitle);
        sheet.setColumnWidth(3, 10000);
        rowhead.createCell(4).setCellValue("İlaç Adı");
        rowhead.getCell(4).setCellStyle(csTableTitle);
        sheet.setColumnWidth(4, 10000);
        rowhead.createCell(5).setCellValue("Miktar");
        rowhead.getCell(5).setCellStyle(csTableTitle);
        sheet.setColumnWidth(5, 3000);

        //A4 Sayfaya Sığdırma
        sheet.setFitToPage(true);
        PrintSetup ps = sheet.getPrintSetup();
        ps.setFitWidth( (short) 1);
        ps.setFitHeight( (short) 0);

    }

    public Page<CustomerSupplyOrderExcelDto> searchReportBuyExcel(String authHeader, Pageable page, AccountingReportSearchDto dto) throws NotFoundException {

        StringBuilder createSqlQuery = new StringBuilder("select * from customer_supply_order cso ");
        createSqlQuery.append("join receipt r on r.receipt_id = cso.receipt_id ");
        createSqlQuery.append("join final_receipt fr on fr.final_receipt_id = r.final_receipt_id ");
        createSqlQuery.append("join invoice i on i.invoice_id = fr.invoice_id ");
        createSqlQuery.append("where i.invoice_status_id >= 10 ");

        if (dto.getStartDate() != null )
            createSqlQuery.append(" and i.created_at  >= to_date('" + sdf.format(dto.getStartDate()) + "'," + "'dd.MM.yyyy') ");
        if (dto.getEndDate() != null )
            createSqlQuery.append(" and i.created_at  < to_date('" + sdf.format(dto.getEndDate()) + "'," + "'dd.MM.yyyy') ");
        if(dto.getSupplyOrderNo() != null)
            if (dto.getSupplyOrderNo().trim().length() > 0)
                createSqlQuery.append(" and cso.supply_order_no = '" +dto.getSupplyOrderNo().trim() +"'");
        createSqlQuery.append(" order by cso.customer_supply_order_id");

        List<CustomerSupplyOrder> list = entityManager.createNativeQuery(createSqlQuery.toString(), CustomerSupplyOrder.class).getResultList();
        CustomerSupplyOrderExcelDto[] dtos = mapper.map(list, CustomerSupplyOrderExcelDto[].class);
        List<CustomerSupplyOrderExcelDto> dtosList = Arrays.asList(dtos);

        int start = Math.min((int) page.getOffset(), dtosList.size());
        int end = Math.min((start + page.getPageSize()), dtosList.size());

        Page<CustomerSupplyOrderExcelDto> pageList = new PageImpl<>(dtosList.subList(start, end), page, dtosList.size());

        return pageList;
    }

    public String createReportBuyExcel(String authHeader, AccountingReportSearchDto dto) throws IOException, NotFoundException {
        User user = controlService.getUserFromToken(authHeader);

        StringBuilder createSqlQuery = new StringBuilder("select * from customer_supply_order cso ");
        createSqlQuery.append("join receipt r on r.receipt_id = cso.receipt_id ");
        createSqlQuery.append("join final_receipt fr on fr.final_receipt_id = r.final_receipt_id ");
        createSqlQuery.append("join invoice i on i.invoice_id = fr.invoice_id ");
        createSqlQuery.append("where i.invoice_status_id >= 10 ");

        if (dto.getStartDate() != null )
            createSqlQuery.append(" and i.created_at  >= to_date('" + sdf.format(dto.getStartDate()) + "'," + "'dd.MM.yyyy') ");
        if (dto.getEndDate() != null )
            createSqlQuery.append(" and i.created_at  < to_date('" + sdf.format(dto.getEndDate()) + "'," + "'dd.MM.yyyy') ");
        if(dto.getSupplyOrderNo() != null)
            if (dto.getSupplyOrderNo().trim().length() > 0)
                createSqlQuery.append(" and cso.supply_order_no = '" +dto.getSupplyOrderNo().trim() +"'");
        createSqlQuery.append(" order by cso.customer_supply_order_id");

        List<CustomerSupplyOrder> list = entityManager.createNativeQuery(createSqlQuery.toString(), CustomerSupplyOrder.class).getResultList();
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

        addCustomerSupplyOrderExcelHeader(workbook,sheet);

        int a = 4;
        for (CustomerSupplyOrder data : list) {
            a++;
            XSSFRow row = sheet.createRow((short) a);

            row.createCell(0).setCellValue(sdf.format(data.getReceipt().getInvoice().getCreatedAt()));
            row.getCell(0).setCellStyle(csBody);

            row.createCell(1).setCellValue(data.getReceipt().getInvoice().getInvoiceNo());
            row.getCell(1).setCellStyle(csBody);

            row.createCell(2).setCellValue(data.getSupplier().getSupplierName());
            row.getCell(2).setCellStyle(csBody);

                row.createCell(3).setCellValue(data.getDrugCard().getDrugName());
                row.getCell(3).setCellStyle(csBody);

            Double value = ((double) ((int) (data.getTotalQuantity() * 1000.0))) / 1000.0;

            row.createCell(4).setCellValue(value);
            row.getCell(4).setCellStyle(csBody);

            value = ((double) ((int) (data.getUnitPrice() * 1000.0))) / 1000.0;

            row.createCell(5).setCellValue(value);
            row.getCell(5).setCellStyle(csBody);

          //  value = ((double) ((int) (data.getSupplyOrderPrice().getVatSum() * 1000.0))) / 1000.0;

            row.createCell(6).setCellValue(value);
            row.getCell(6).setCellStyle(csBody);
        }

        String excelTitle="activity-excel_"+user.getUserId();

        FileOutputStream fileOut = new FileOutputStream("docs/"+excelTitle+".xlsx");
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();

        return excelTitle;
    }

    private void addCustomerSupplyOrderExcelHeader(XSSFWorkbook workbook, XSSFSheet sheet) throws IOException {

        CellStyle csHeader = this.createCSHeaderForExcelHeader(workbook);
        //CellStyle csBody = this.createCSBodyForExcelHeader(workbook);
        CellStyle csTableTitle = this.createCSTableTitleForExcelHeader(workbook);

        //Başlık verilir
        XSSFRow rowHeader = sheet.createRow((short) 0);
        rowHeader.createCell(0).setCellValue("RAPOR : ILAC ALIS RAPORU (DETAYLI)");
        rowHeader.getCell(0).setCellStyle(csHeader);


        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 1));
        XSSFRow rowDate = sheet.createRow((short) 2);

        //Bu kısım Kolon Sayısına Eşit Olmalı
        rowDate.createCell(0).setCellValue(sdf.format(new Date()));
        rowDate.getCell(0).setCellStyle(csHeader);


        XSSFRow rowhead = sheet.createRow((short) 4);

        rowhead.createCell(0).setCellValue("TARİH");
        rowhead.getCell(0).setCellStyle(csTableTitle);
        sheet.setColumnWidth(0, 3000);
        rowhead.createCell(1).setCellValue("FATURA NO");
        rowhead.getCell(1).setCellStyle(csTableTitle);
        sheet.setColumnWidth(1, 7000);
        rowhead.createCell(2).setCellValue("FIRMA");
        rowhead.getCell(2).setCellStyle(csTableTitle);
        sheet.setColumnWidth(2, 8500);
        rowhead.createCell(3).setCellValue("ILAC ADI");
        rowhead.getCell(3).setCellStyle(csTableTitle);
        sheet.setColumnWidth(3, 10000);
        rowhead.createCell(4).setCellValue("MIKTARI");
        rowhead.getCell(4).setCellStyle(csTableTitle);
        sheet.setColumnWidth(4, 2500);
        rowhead.createCell(5).setCellValue("FIYATI");
        rowhead.getCell(5).setCellStyle(csTableTitle);
        sheet.setColumnWidth(5, 2500);
        rowhead.createCell(6).setCellValue("KDV");
        rowhead.getCell(6).setCellStyle(csTableTitle);
        sheet.setColumnWidth(6, 2500);

        //A4 Sayfaya Sığdırma
        sheet.setFitToPage(true);
        PrintSetup ps = sheet.getPrintSetup();
        ps.setFitWidth( (short) 1);
        ps.setFitHeight( (short) 0);

    }

    private CellStyle createCSHeaderForExcelHeader(XSSFWorkbook workbook){
        //STYLE BAŞLANGIÇ
        XSSFFont fontHeader = workbook.createFont();
        fontHeader.setFontHeightInPoints((short) 12);
        fontHeader.setFontName("Times New Roman");
        CellStyle csHeader = workbook.createCellStyle();
//        csHeader.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
//        csHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        csHeader.setFont(fontHeader);
        csHeader.setWrapText(true);
        csHeader.setLocked(false);
        csHeader.setAlignment(HorizontalAlignment.LEFT);
        csHeader.setFont(fontHeader);
        return  csHeader;
    }
    private CellStyle createCSBodyForExcelHeader(XSSFWorkbook workbook){
        //STYLE BAŞLANGIÇ
        CellStyle csBody = workbook.createCellStyle();
        XSSFFont fontBody = workbook.createFont();
        fontBody.setFontName("Times New Roman");
        fontBody.setFontHeightInPoints((short) 11);
        csBody.setWrapText(true);
        csBody.setLocked(false);
        csBody.setFont(fontBody);
        csBody.setAlignment(HorizontalAlignment.CENTER);
        return  csBody;
    }
    private CellStyle createCSTableTitleForExcelHeader(XSSFWorkbook workbook){
        //STYLE BAŞLANGIÇ
        CellStyle csTableTitle = workbook.createCellStyle();
        XSSFFont fontBody = workbook.createFont();
        fontBody.setFontName("Times New Roman");
        fontBody.setFontHeightInPoints((short) 11);
        csTableTitle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
        csTableTitle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        csTableTitle.setWrapText(true);
        csTableTitle.setLocked(false);
        csTableTitle.setFont(fontBody);
        csTableTitle.setAlignment(HorizontalAlignment.CENTER);
        return  csTableTitle;
    }
}
