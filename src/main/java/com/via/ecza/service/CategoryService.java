package com.via.ecza.service;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.via.ecza.dto.*;
import com.via.ecza.entity.*;
import com.via.ecza.repo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@Transactional
public class CategoryService {

    @Autowired
    private AccountingCodeRepository accountingCodeRepository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CheckingCardRepository checkingCardRepository;
    @Autowired
    private ControlService controlService;
    @Autowired
    private ReceiptRepository receiptRepository;




    public Page<CategoryDto> searchCategoryCode(Pageable page, CategorySearchDto dto) {

        StringBuilder createSqlQuery = new StringBuilder("select * from category c where c.status =1 ");
        if (dto.getCode() != null) createSqlQuery.append("and c.code ILIKE '%" + dto.getCode().trim() + "%' ");
        if (dto.getName() != null) createSqlQuery.append("and c.name ILIKE '%" + dto.getName().trim() + "%' ");
        if (dto.getCodeValue() != null) createSqlQuery.append("and c.code_value ILIKE '%" + dto.getCodeValue().trim() + "%' ");
        createSqlQuery.append(" order by c.code_value ");

        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Category.class).getResultList();
        CategoryDto[] dtos = mapper.map(list, CategoryDto[].class);
        List<CategoryDto> dtosList = Arrays.asList(dtos);
        int start = Math.min((int) page.getOffset(), dtosList.size());
        int end = Math.min((start + page.getPageSize()), dtosList.size());

        Page<CategoryDto> pageList = new PageImpl<>(dtosList.subList(start, end), page, dtosList.size());

        return pageList;
    }
    public List<CategoryDto> findBySearching(String categoryName) {
        List<CategoryDto> dtoList = new ArrayList<>();
        if(categoryName.length()>=0){
            StringBuilder createSqlQuery = new StringBuilder("select * from category where 1=1 ");
            if(categoryName.trim().length()>0){
                createSqlQuery.append("and name ILIKE '%"+(categoryName.trim()).toUpperCase()+"%' ");
            }
            createSqlQuery.append("order by code_value ASC");
            List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Category.class).getResultList();
            CategoryDto[] dtos = mapper.map(list,CategoryDto[].class );
            dtoList = Arrays.asList(dtos);
            return dtoList;
        }else{
            return dtoList;
        }
    }
    public List<CategoryDto> findBySearchingCode(String categoryCode) {
        List<CategoryDto> dtoList = new ArrayList<>();
        if(categoryCode.length()>=0){
            StringBuilder createSqlQuery = new StringBuilder("select * from category where 1=1 ");
            if(categoryCode.trim().length()>0){
                createSqlQuery.append("and code_value ILIKE '%"+(categoryCode.trim()).toUpperCase()+"%' ");
            }
            createSqlQuery.append("order by code_value ASC");
            List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Category.class).getResultList();
            CategoryDto[] dtos = mapper.map(list,CategoryDto[].class );
            dtoList = Arrays.asList(dtos);
            return dtoList;
        }else{
            return dtoList;
        }
    }


    public Boolean save(CategorySaveDto dto) throws Exception {
        Category category = new Category();

        Optional<Category> c = null;
        if (dto.getCategoryParentId() != null)
            c = categoryRepository.findById(dto.getCategoryParentId());


        if (c != null)
            if (c.isPresent()) {
                category.setParentCategory(c.get());
                if (c.get() != null)
                    category.setCodeValue(c.get().getCodeValue() + ".");
            }


       Boolean controlSingularity=controlCategorySingularity(dto);
            if(controlSingularity==false){
                //Aynı Muhasebe Kodu Tekrar Edilemez
                return false;
            }



        category.setCode(dto.getCode().trim());
        category.setCreatedDate(new Date());
        category.setName(dto.getName().trim());
        if (category.getCodeValue() != null)
            category.setCodeValue(category.getCodeValue() + dto.getCode());
        else
            category.setCodeValue(dto.getCode().trim());
        category.setStatus(1);

        if(dto.getCheckingCardId() != null){
            Optional<CheckingCard> optionalCheckingCard = checkingCardRepository.findById(dto.getCheckingCardId());
            CheckingCard checkingCard = optionalCheckingCard.get();
            category.setCheckingCard(checkingCardRepository.findById(dto.getCheckingCardId()).get());

        }

        category = categoryRepository.save(category);



        return true;
    }

    private String getCategoryCode(String data, Category c) {
        if (c == null)
            return "";
        else
            return c.getCode() + data;
    }

    public Boolean update(CategorySaveDto dto) throws Exception {

        try {
            Category category = categoryRepository.findById(dto.getCategoryId()).get();
            String oldCodeValue = category.getCodeValue();


//            Boolean controlSingularity=controlCategorySingularity(dto);
//            if(controlSingularity==false){
//                //Aynı Muhasebe Kodu Tekrar Edilemez
//                return false;
//            }


            if (dto.getCategoryParentId() == null) {
                //Ana kategori olarak güncelleniyorsa
                category.setParentCategory(null);
                category.setCodeValue(dto.getCode().trim());
                category.setCode(dto.getCode().trim());
                category.setName(dto.getName().trim());

                //Alt Kategorilerin Parent'ı Değişecek
                List<Category> list = categoryRepository.getSubCategoriesToCodeValue(oldCodeValue);
                for (Category categories : list) {
                    CategorySaveDto controlDto=new CategorySaveDto();
                    controlDto.setCategoryParentId(null);
                    controlDto.setCode(category.getCode().trim());

                    Boolean controlSingularitySubCategories=controlCategorySingularityInForLoop(controlDto);
                    if(controlSingularitySubCategories==false){
                        //Aynı Muhasebe Kodu Tekrar Edilemez
                        return false;
                    }

                    categories.setCodeValue(categories.getCodeValue().replace(oldCodeValue+".", category.getCode()+"."));
                    categoryRepository.save(categories);
                }

                categoryRepository.save(category);

            } else if (dto.getCategoryParentId() != null) {
                //Parent'ı farklı kategori seçildiyse
                Category newParent = categoryRepository.findById(dto.getCategoryParentId()).get();
                category.setParentCategory(newParent);
                category.setCode(dto.getCode().trim());
                category.setName(dto.getName().trim());
                category.setCodeValue(newParent.getCodeValue() + "." + dto.getCode());

                //Alt Kategorilerin Parent'ı Değişecek
                List<Category> list = categoryRepository.getSubCategoriesToCodeValue(oldCodeValue);
                for (Category categories : list) {
                    CategorySaveDto controlDto=new CategorySaveDto();
                    controlDto.setCategoryParentId(category.getParentCategory().getCategoryId());
                    controlDto.setCode(category.getCode());

                    Boolean controlSingularitySubCategories=controlCategorySingularityInForLoop(controlDto);
                    if(controlSingularitySubCategories==false){
                        //Aynı Muhasebe Kodu Tekrar Edilemez
                       return false;
                    }

                    categories.setCodeValue(categories.getCodeValue().replace(oldCodeValue, category.getCodeValue()));
                    categoryRepository.save(categories);
                }

                categoryRepository.save(category);

            }


        } catch (Exception e) {
            throw new Exception("Kategori Güncelleme Sırasında Hata Oluştu.", e);
        }

        return true;
    }


    public SingleCategoryDto findById(Long categoryId) {


        SingleCategoryDto dto = null;
        Optional<Category> ca = categoryRepository.findById(categoryId);
        if (ca.isPresent())
            dto = mapper.map(ca.get(), SingleCategoryDto.class);

        return dto;
    }

    public List<CheckingCardAccountActivityDto> findByParentId(Long categoryId) {
        try {

        StringBuilder createSqlQuery = new StringBuilder("select * from invoice i where i.category_id = " + categoryId);
        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Invoice.class).getResultList();
        CheckingCardAccountActivityDto[] dtos = mapper.map(list,CheckingCardAccountActivityDto[].class );
        List<CheckingCardAccountActivityDto> dtosList  = Arrays.asList(dtos);
        return dtosList;
    } catch (Exception e) {
        throw e;
    }
    }

    public List<CategoryDto> getAllCategories() {

        List<Category> list = categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "codeValue"));

        CategoryDto[] dtos = mapper.map(list, CategoryDto[].class);
        List<CategoryDto> dtosList = Arrays.asList(dtos);
        return dtosList;
    }

    private Boolean controlCategorySingularity (CategorySaveDto dto) {
        int control=-1;

        if(dto.getCategoryParentId()==null){
            control=categoryRepository.findByCodeAndNullCategory(dto.getCode());
        }else{
            control=categoryRepository.findByCodeAndCategory(dto.getCode(),dto.getCategoryParentId());
        }

        if(control>0){
            //Tekrar Eden Muhasebe Kaydı Var
            return false;
        }else{
            //Tekrar Eden Muhasebe Kaydı Yok
            return true;
        }
    }

    private Boolean controlCategorySingularityInForLoop (CategorySaveDto dto) {
        int control=-1;

        if(dto.getCategoryParentId()==null){
            control=categoryRepository.findByCodeAndNullCategory(dto.getCode());
        }else{
            control=categoryRepository.findByCodeAndCategory(dto.getCode(),dto.getCategoryParentId());
        }

        if(control>1){
            //Tekrar Eden Muhasebe Kaydı Var
            return false;
        }else{
            //Tekrar Eden Muhasebe Kaydı Yok
            return true;
        }
    }

    public List<CategoryDto> getCategoriesForBuyUtilityInvoice() {
        List<Category> list = categoryRepository.getCategoriesForBuyUtilityInvoice();
        CategoryDto[] dtos = mapper.map(list, CategoryDto[].class);
        List<CategoryDto> dtosList = Arrays.asList(dtos);
        return dtosList;
    }

    public List<CategoryDto> getCategoriesForSellUtilityInvoice() {
        List<Category> list = categoryRepository.getCategoriesForSellUtilityInvoice();
        CategoryDto[] dtos = mapper.map(list, CategoryDto[].class);
        List<CategoryDto> dtosList = Arrays.asList(dtos);
        return dtosList;
    }

    public List<CategoryDto> getVatCategoriesForBuyUtilityInvoice() {
        List<Category> list = categoryRepository.getVatCategoriesForBuyUtilityInvoice();
        CategoryDto[] dtos = mapper.map(list, CategoryDto[].class);
        List<CategoryDto> dtosList = Arrays.asList(dtos);
        for (CategoryDto dto:dtosList) {
            String parseVat=dto.getName().substring(1,dto.getName().indexOf(" "));
            if(parseVat.matches("[0-9]+") && parseVat.length() > 0){
                dto.setVatValue(Integer.valueOf(parseVat));
            }
        }
        return dtosList;
    }
    public List<CategoryDto> getVatCategoriesForSellUtilityInvoice() {
        List<Category> list = categoryRepository.getVatCategoriesForSellUtilityInvoice();
        CategoryDto[] dtos = mapper.map(list, CategoryDto[].class);
        List<CategoryDto> dtosList = Arrays.asList(dtos);
        for (CategoryDto dto:dtosList) {
            String parseVat=dto.getName().substring(1,dto.getName().indexOf(" "));
            if(parseVat.matches("[0-9]+") && parseVat.length() > 0){
                dto.setVatValue(Integer.valueOf(parseVat));
            }
        }
        return dtosList;
    }
    public List<CategoryDto> getCommercialProductsCategories() {
        List<Category> list = categoryRepository.getCommercialProductsCategories();
        CategoryDto[] dtos = mapper.map(list, CategoryDto[].class);
        List<CategoryDto> dtosList = Arrays.asList(dtos);
        return dtosList;
    }
    public List<CategoryDto> getDomesticSellCategories() {
        List<Category> list = categoryRepository.getDomesticSellCategories();
        CategoryDto[] dtos = mapper.map(list, CategoryDto[].class);
        List<CategoryDto> dtosList = Arrays.asList(dtos);
        return dtosList;
    }
    public List<AccountingCodeDetailListDto> getAccountingCodeDetailListDto(Long  singleCategoryId){


            StringBuilder createSqlQuery = new StringBuilder("select * from account_activity where category_id ="+singleCategoryId);
            List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), AccountActivity.class).getResultList();
            AccountingCodeDetailListDto[] dtos = mapper.map(list,AccountingCodeDetailListDto[].class );
            List<AccountingCodeDetailListDto> dtosList  = Arrays.asList(dtos);
            return dtosList;

    }

    public String createBuyAccountingPdf(String authHeader, Long singleCategoryId) throws Exception {

        try {
            /* Belge sonuna kullanıcı Id'si eklmek için kullanıcı bilgisi alınıyor */
            User user = controlService.getUserFromToken(authHeader);
            StringBuilder createSqlQuery = new StringBuilder("select * from account_activity where category_id ="+singleCategoryId);
            List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), AccountActivity.class).getResultList();
            AccountingCodeDetailListDto[] dtos = mapper.map(list,AccountingCodeDetailListDto[].class );
            List<AccountingCodeDetailListDto> dtosList  = Arrays.asList(dtos);


            //PDF BAŞLANGIÇ
            String fileName = "muhasebe" + "-" + user.getUserId() + ".pdf";
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("docs/" + fileName));
            document.open();


            Image image1 = Image.getInstance("image/logo/pharma.png");
            image1.setAlignment(Element.ALIGN_LEFT);
            image1.scaleAbsolute(60, 60);
            document.add(image1);

            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Paragraph date = new Paragraph(dateFormat.format(new Date()));
            date.setAlignment(Element.ALIGN_RIGHT);
            document.add(date);

            document.add(new Paragraph("\n"));

            BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
            com.itextpdf.text.Font catFont = new com.itextpdf.text.Font(bf, 16, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);

            //Tablonun Başlığı Girilir
            Paragraph tableHeader = new Paragraph("Muhasebe Kodu Bilgisi", catFont);

            tableHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(tableHeader);

            document.add(new Paragraph("\n"));

            //Tablonun Sütun Sayısı Girilir
            PdfPTable table = new PdfPTable(5);
            table.setWidths(new int[]{2, 6, 4, 3, 3});

            table.setWidthPercentage(100);
            addTableHeader(table);

            //Hücrelere Veriler Girilir
            int a = 0;
            for  (AccountingCodeDetailListDto act : dtosList){
                a++;
                addRows(table, String.valueOf(a));
                addRows(table, act.getCheckingCard().getCheckingCardName());
                addRows(table, act.getInvoice().getInvoiceNo());
                addRows(table, act.getOtherCheckingCard().getCheckingCardName());
                addRows(table, dateFormat.format(act.getCreatedAt()));

            }

            document.add(table);
            document.close();
            //PDF SON
            int index = fileName.indexOf(".pdf");
            fileName=fileName.substring(0,index);
            return fileName;

        } catch (Exception e) {
            throw new Exception("Fiş Pdf Oluşturma İşleminde Hata Oluştu.", e);
        }
    }

    private void addTableHeader(PdfPTable table) throws IOException, DocumentException {
        BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
        com.itextpdf.text.Font catFont = new com.itextpdf.text.Font(bf, 10, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);

        //Tablo Sütün Başlıkları Girilir
        Stream.of("No ","Firma İsim", "Fatura No", "Muşteri İsim", "Tarih")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(1);
                    header.setPhrase(new Phrase(columnTitle, catFont));
                    header.setPadding(3);


                    table.addCell(header);

                });
    }
    private void addRows(PdfPTable table, String value) throws IOException, DocumentException {
        BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
        com.itextpdf.text.Font catFont = new com.itextpdf.text.Font(bf, 8, Font.NORMAL, BaseColor.BLACK);


        table.addCell(new Phrase(value, catFont));
    }

        private void addRows2(PdfPTable table, String value) throws IOException, DocumentException {
            BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
            com.itextpdf.text.Font catFont = new com.itextpdf.text.Font(bf, 8, Font.NORMAL, BaseColor.BLACK);


            table.addCell(new Phrase(value, catFont));


    }

    public String createBuyAccountingExcel(String authHeader, Long singleCategoryId) throws Exception {

        try {
            Optional<Object> result = null;
            User user = controlService.getUserFromToken(authHeader);
            Optional<Category> optionalCategory = categoryRepository.findById(singleCategoryId);
            if (!optionalCategory.isPresent())
                throw new Exception("Böyle bir fiş yoktur");

            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Sayfa1");

            //STYLE BAŞLANGIÇ
            CellStyle csBody = workbook.createCellStyle();
            XSSFFont fontBody = workbook.createFont();
            fontBody.setFontName("Times New Roman");
            fontBody.setFontHeightInPoints((short) 7);
            csBody.setWrapText(true);
            csBody.setLocked(false);
            csBody.setFont(fontBody);
            //STYLE SON

            addExcelHeader(workbook, sheet);
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            int a = 5;
            int b = 0;
            for (AccountingCodeDetailListDto act : getAccountingCodeDetailListDto(singleCategoryId)){
                a++;
                b++;
                XSSFRow row = sheet.createRow((short) a);
                row.createCell(0).setCellValue(b);
                row.getCell(0).setCellStyle(csBody);
                //İçerik Kısmı Bu Satırdan İtibaren Obje Bilgileri İle Doldurulur
                row.createCell(1).setCellValue(act.getCheckingCard().getCheckingCardName());
                row.getCell(1).setCellStyle(csBody);
                row.createCell(2).setCellValue(act.getInvoice().getInvoiceNo());
                row.getCell(2).setCellStyle(csBody);
                row.createCell(3).setCellValue(act.getOtherCheckingCard().getCheckingCardName());
                row.getCell(3).setCellStyle(csBody);
                row.createCell(4).setCellValue(dateFormat.format(act.getCreatedAt()));
                row.getCell(4).setCellStyle(csBody);


            }


            FileOutputStream fileOut = new FileOutputStream(
                    "docs/" + "MuhasebeKodListesi" + "-" + user.getUserId() + ".xlsx");
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
            String fileName = "MuhasebeKodListesi" + "-" + user.getUserId();



            return fileName;
        } catch (Exception e) {
            throw new Exception("Fiş Excel Oluşturma İşleminde Hata Oluştu.", e);
        }
    }

    private void addExcelHeader(XSSFWorkbook workbook, XSSFSheet sheet) throws IOException {
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
//        csHeading.setFillForegroundColor(IndexedColors.BLUE_GREY.index);

        CellStyle csDate = workbook.createCellStyle();
        XSSFFont fontDate = workbook.createFont();
        fontDate.setFontName("Times New Roman");
        fontDate.setFontHeightInPoints((short) 12);
        csDate.setFont(fontDate);
        csDate.setLocked(false);
        //STYLE SON

        XSSFRow rowDate = sheet.createRow((short) 2);
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        //Bu kısım Kolon Sayısına Eşit Olmalı
        rowDate.createCell(7).setCellValue(dateFormat.format(new Date()));
        rowDate.getCell(7).setCellStyle(csDate);


        InputStream inputStream = new FileInputStream("image/logo/pharma.png");
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
        rowHeader.createCell(0).setCellValue("Muhasebe Kodu Bilgileri");
        rowHeader.getCell(0).setCellStyle(csHeading);

        sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 7));
        //son parametre kolon sayısına eşit olmalı
//        sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 9));


        XSSFRow rowhead = sheet.createRow((short) 5);

        //Kolon İsimleri Belirtilir İhtiyaç Olursa Kolonlar Genişletilir

        rowhead.createCell(0).setCellValue("No");
        rowhead.getCell(0).setCellStyle(csHeader);
        sheet.setColumnWidth(0, 1500);//kolon genişletildi
        //Kolon İsimleri Belirtilir İhtiyaç Olursa Kolonlar Genişletilir

        rowhead.createCell(1).setCellValue("Firma İsim");
        rowhead.getCell(1).setCellStyle(csHeader);
        sheet.setColumnWidth(1, 4500);//kolon genişletildi

        rowhead.createCell(2).setCellValue("Fatura No");
        rowhead.getCell(2).setCellStyle(csHeader);
        sheet.setColumnWidth(2, 5500);

        rowhead.createCell(3).setCellValue("Müşteri İsim");
        rowhead.getCell(3).setCellStyle(csHeader);
        sheet.setColumnWidth(3, 3000);

        rowhead.createCell(4).setCellValue("Tarih");
        rowhead.getCell(4).setCellStyle(csHeader);
        sheet.setColumnWidth(4, 4000);

        //A4 Sayfaya Sığdırma
        sheet.setFitToPage(true);
        PrintSetup ps = sheet.getPrintSetup();
        ps.setFitWidth((short) 1);
        ps.setFitHeight((short) 0);

    }









}
