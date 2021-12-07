package com.via.ecza.service;


import com.via.ecza.dto.PriceDto;
import com.via.ecza.dto.PriceSearchDto;
import com.via.ecza.entity.DrugCard;
import com.via.ecza.entity.Price;
import com.via.ecza.entity.User;
import com.via.ecza.entity.enumClass.Role;
import com.via.ecza.repo.DrugCardRepository;
import com.via.ecza.repo.PriceRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.*;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
public class PriceService {


    @Autowired
    private DrugCardRepository drugCardRepository;
    @Autowired
    private PriceRepository priceRepository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ControlService controlService;

    public Page<PriceDto> search(PriceSearchDto dto, Integer pageNo, Integer pageSize, String sortBy) {
        StringBuilder createSqlQuery = new StringBuilder("select * from drug_card dc inner join price p on dc.drug_card_id =p.drug_card_id " +
                " inner join discount d on d.drug_card_id = dc.drug_card_id where 1=1 ");
        if (dto.getDrugCardId() != null) createSqlQuery.append("and dc.drug_card_id =" + dto.getDrugCardId() + " ");
        if (dto.getDrugCode() != null) createSqlQuery.append("and dc.drug_code = " + dto.getDrugCode() + " ");
        if (dto.getDepotSalePriceExcludingVat() != null) {
            createSqlQuery.append("and p.depot_sale_price_excluding_vat-0.1 <=" + dto.getDepotSalePriceExcludingVat() + " ");
            createSqlQuery.append("and p.depot_sale_price_excluding_vat+0.1 >=" + dto.getDepotSalePriceExcludingVat() + " ");
        }
        if (dto.getDepotSalePriceExcludingVatWithInstutionDiscount() != null) {
            createSqlQuery.append("and (p.depot_sale_price_excluding_vat- (p.depot_sale_price_excluding_vat*d.instution_discount / 100)-0.1 ) <=" + dto.getDepotSalePriceExcludingVatWithInstutionDiscount() + " ");
            createSqlQuery.append("and (p.depot_sale_price_excluding_vat- (p.depot_sale_price_excluding_vat*d.instution_discount / 100)+0.1 ) >=" + dto.getDepotSalePriceExcludingVatWithInstutionDiscount() + " ");
        }

        if (pageNo == 0) {
            createSqlQuery.append("order by dc.drug_name limit " + pageSize + " offset " + pageNo);
        } else {
            createSqlQuery.append("order by dc.drug_name limit " + pageSize + " offset " + (pageSize * pageNo));
        }


        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Price.class).getResultList();

        PriceDto[] dtos = mapper.map(list, PriceDto[].class);
        List<PriceDto> dtosList = Arrays.asList(dtos);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
        /*int start = Math.min((int) paging.getOffset(), dtosList.size());
        int end = Math.min((start + paging.getPageSize()), dtosList.size());*/

        int start = 0;
        int end = dtosList.size();
        int totalCount = 0;

        //Arama Kriterlerine Göre Toplam Kayıt Sayısı Başlangıç
        StringBuilder createSqlQueryCount = new StringBuilder("select count(*) from drug_card dc inner join price p on dc.drug_card_id =p.drug_card_id " +
                " inner join discount d on d.drug_card_id = dc.drug_card_id where 1=1 ");
        if (dto.getDrugCardId() != null)
            createSqlQueryCount.append("and dc.drug_card_id =" + dto.getDrugCardId() + " ");
        if (dto.getDrugCode() != null) createSqlQueryCount.append("and dc.drug_code = " + dto.getDrugCode() + " ");
        if (dto.getDepotSalePriceExcludingVat() != null) {
            createSqlQueryCount.append("and p.depot_sale_price_excluding_vat-0.1 <=" + dto.getDepotSalePriceExcludingVat() + " ");
            createSqlQueryCount.append("and p.depot_sale_price_excluding_vat+0.1 >=" + dto.getDepotSalePriceExcludingVat() + " ");
        }
        if (dto.getDepotSalePriceExcludingVatWithInstutionDiscount() != null) {
            createSqlQueryCount.append("and (p.depot_sale_price_excluding_vat- (p.depot_sale_price_excluding_vat*d.instution_discount / 100)-0.1 ) <=" + dto.getDepotSalePriceExcludingVatWithInstutionDiscount() + " ");
            createSqlQueryCount.append("and (p.depot_sale_price_excluding_vat- (p.depot_sale_price_excluding_vat*d.instution_discount / 100)+0.1 ) >=" + dto.getDepotSalePriceExcludingVatWithInstutionDiscount() + " ");
        }

        List<Object> countList = entityManager.createNativeQuery(createSqlQueryCount.toString()).getResultList();

        for (Object data : countList) {
            totalCount = Integer.valueOf(String.valueOf((BigInteger) data));
        }
        //Arama Kriterlerine Göre Toplam Kayıt Sayısı Son

        Page<PriceDto> pageList = new PageImpl<>(dtosList.subList(start, end), paging, totalCount);

        return pageList;
    }

    public PriceDto priceDetail(Long priceId) {
        Optional<Price> optionalPrice = priceRepository.findById(priceId);
        Price p = optionalPrice.get();
        PriceDto dto = mapper.map(p, PriceDto.class);
        return dto;
    }


    public Boolean UpdateFromExcelForNewFormat(String authHeader, String fileName) throws Exception {

        User user = controlService.getUserFromToken(authHeader);
        if (user.getRole() != Role.ADMIN && user.getRole() != Role.MANAGER) {
            throw new Exception("Bu Alanda Sadece Admin ve Müdür Yetkisi Vardır");
        }

        try {

            //Excel Boyut Düşürme Gereksiz Sekmeler Siliniyor
            Boolean result = deleteFromExcelUnnecessaryTabs(fileName);
            if (result == false) {
                throw new Exception("Excel gereksiz sekme silme işleminde hata oluştu !");
            }

            //yeni excel okundu
            HashMap<Long, PriceDto> newMap = readNewVersionExcel(fileName);

            //eski excel okundu
            HashMap<Long, PriceDto> oldMap = readNewVersionExcel(fileName + "Old");

            if (newMap.size() <= 0 || oldMap.size() <= 0) {
                throw new Exception("Eski veya Yeni Excel Okunamadı !");
            }

            newMap.keySet().stream().forEach(x -> {
                //eski map te key yoksa yeni kayıttır
                if (oldMap.containsKey(x)) {
                    //true dönerse değişiklik yapılmamıştır
                    if (!dateControl(newMap.get(x).getDateOfChange(), oldMap.get(x).getDateOfChange())) {
                        try {
                            saveOrUpdatePriceFromExcel(newMap.get(x));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    try {
                        saveOrUpdatePriceFromExcel(newMap.get(x));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public Boolean UpdateFromExcelForOldFormat(String authHeader, String fileName) throws Exception {

        User user = controlService.getUserFromToken(authHeader);
        if (user.getRole() != Role.ADMIN && user.getRole() != Role.MANAGER) {
            throw new Exception("Bu Alanda Sadece Admin ve Müdür Yetkisi Vardır");
        }

        try {

            //Excel Boyut Düşürme Gereksiz Sekmeler Siliniyor
            Boolean result = deleteFromExcelUnnecessaryTabs(fileName);
            if (result == false) {
                throw new Exception("Excel gereksiz sekme silme işleminde hata oluştu !");
            }

            //yeni excel okundu
            HashMap<Long, PriceDto> newMap = readOldVersionExcel(fileName);

            //eski excel okundu
            HashMap<Long, PriceDto> oldMap = readOldVersionExcel(fileName + "Old");

            if (newMap.size() <= 0 || oldMap.size() <= 0) {
                throw new Exception("Eski veya Yeni Excel Okunamadı !");
            }

            newMap.keySet().stream().forEach(x -> {
                //eski map te key yoksa yeni kayıttır
                if (oldMap.containsKey(x)) {
                    //true dönerse değişiklik yapılmamıştır
                    if (!dateControl(newMap.get(x).getDateOfChange(), oldMap.get(x).getDateOfChange())) {
                        try {
                            saveOrUpdatePriceFromExcel(newMap.get(x));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    try {
                        saveOrUpdatePriceFromExcel(newMap.get(x));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }


    public int createPriceExcel(String authHeader, PriceSearchDto dto) throws Exception {

        try {
            //ARAMA BAŞLANGIÇ
            StringBuilder createSqlQuery = new StringBuilder("select * from drug_card dc inner join price p on dc.drug_card_id =p.drug_card_id " +
                    " inner join discount d on d.drug_card_id = dc.drug_card_id where dc.status = 1 ");
            if (dto.getDrugCardId() != null) createSqlQuery.append("and dc.drug_card_id =" + dto.getDrugCardId() + " ");
            if (dto.getDrugCode() != null) createSqlQuery.append("and dc.drug_code = " + dto.getDrugCode() + " ");
            if (dto.getDepotSalePriceExcludingVat() != null)
                createSqlQuery.append("and p.depot_sale_price_excluding_vat = " + dto.getDepotSalePriceExcludingVat() + " ");
            if (dto.getDepotSalePriceExcludingVatWithInstutionDiscount() != null) {
                createSqlQuery.append("and (p.depot_sale_price_excluding_vat- (p.depot_sale_price_excluding_vat*d.instution_discount / 100)-0.1 ) <=" + dto.getDepotSalePriceExcludingVatWithInstutionDiscount() + " ");
                createSqlQuery.append("and (p.depot_sale_price_excluding_vat- (p.depot_sale_price_excluding_vat*d.instution_discount / 100)+0.1 ) >=" + dto.getDepotSalePriceExcludingVatWithInstutionDiscount() + " ");
            }
            createSqlQuery.append("order by dc.drug_name");

            List<Price> list = entityManager.createNativeQuery(createSqlQuery.toString(), Price.class).getResultList();

            //ARAMA SON

            /* Belge sonuna kullanıcı Id'si eklmek için kullanıcı bilgisi alınıyor */
            User user = controlService.getUserFromToken(authHeader);

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
            //STYLE SON

            addExcelHeader(workbook, sheet);
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            int a = 6;
            int b = 0;
            for (Price price : list) {
                a++;
                b++;
                XSSFRow row = sheet.createRow((short) a);
                row.createCell(0).setCellValue(b);
                row.getCell(0).setCellStyle(csBody);
                //İçerik Kısmı Bu Satırdan İtibaren Obje Bilgileri İle Doldurulur
                row.createCell(1).setCellValue(price.getDrugCard().getDrugName());
                row.getCell(1).setCellStyle(csBody);
                row.createCell(2).setCellValue("₺ " + price.getDepotSalePriceExcludingVat());
                row.getCell(2).setCellStyle(csBody);
                row.createCell(3).setCellValue("% " + price.getDrugCard().getDiscount().getInstutionDiscount());
                row.getCell(3).setCellStyle(csBody);
                row.createCell(4).setCellValue("₺ " + (price.getDepotSalePriceExcludingVat() - (price.getDepotSalePriceExcludingVat() * price.getDrugCard().getDiscount().getInstutionDiscount() / 100)));
                row.getCell(4).setCellStyle(csBody);


            }

            FileOutputStream fileOut = new FileOutputStream("docs/fiyat_excel_" + user.getUsername() + ".xlsx");
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();

            return 1;
        } catch (Exception e) {
            throw new Exception("Fiyat Excel Oluşturma İşleminde Hata Oluştu.", e);
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
        rowDate.createCell(4).setCellValue(dateFormat.format(new Date()));
        rowDate.getCell(4).setCellStyle(csDate);


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
        rowHeader.createCell(0).setCellValue("Fiyat Listesi");
        rowHeader.getCell(0).setCellStyle(csHeading);

        sheet.addMergedRegion(new CellRangeAddress(0, 2, 0, 1));
        //son parametre kolon sayısına eşit olmalı
        sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 4));


        XSSFRow rowhead = sheet.createRow((short) 6);

        //Kolon İsimleri Belirtilir İhtiyaç Olursa Kolonlar Genişletilir
        rowhead.createCell(0).setCellValue("No");
        rowhead.getCell(0).setCellStyle(csHeader);
        sheet.setColumnWidth(0, 1500);//kolon genişletildi
        rowhead.createCell(1).setCellValue("İlaç Adı");
        rowhead.getCell(1).setCellStyle(csHeader);
        sheet.setColumnWidth(1, 10000);
        rowhead.createCell(2).setCellValue("KDV Hariç Depocu Satış Fiyatı (₺)");
        rowhead.getCell(2).setCellStyle(csHeader);
        sheet.setColumnWidth(2, 6000);
        rowhead.createCell(3).setCellValue("İskonto (%)");
        rowhead.getCell(3).setCellStyle(csHeader);
        sheet.setColumnWidth(3, 6000);
        rowhead.createCell(4).setCellValue("İndirimli Depocu Satış Fiyatı (₺)");
        rowhead.getCell(4).setCellStyle(csHeader);
        sheet.setColumnWidth(4, 6000);

        //A4 Sayfaya Sığdırma
        sheet.setFitToPage(true);
        PrintSetup ps = sheet.getPrintSetup();
        ps.setFitWidth((short) 1);
        ps.setFitHeight((short) 0);
    }

    public HashMap<Long, PriceDto> readOldVersionExcel(String fileName) {
        HashMap<Long, PriceDto> priceMap = new HashMap<>();

        int cellNumber = 0;

        try {
            //System.out.println("Excel Dosyası Okunuyor...");
            FileInputStream file = new FileInputStream(new File("docs/" + fileName + ".xlsx"));
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            // Excel Dosyasının Hangi Sayfası İle Çalışacağımızı Seçelim.
            XSSFSheet sheet = workbook.getSheetAt(0);
            // Belirledigimiz sayfa icerisinde tum satirlari tek tek dolasacak iterator nesnesi
            Iterator rowIterator = sheet.iterator();
            Row row = (Row) rowIterator.next();       //ilk satır başlık olduğu için geçildi

            // Okunacak Satir Oldugu Surece
            while (rowIterator.hasNext()) {
                cellNumber = 0;//yeni satıra geçince hücre sayısı baştan başlıyor
                // Excel içerisindeki satiri temsil eden nesne
                row = (Row) rowIterator.next();
                // Her bir satir icin tum hucreleri dolasacak iterator nesnesi
                Iterator cellIterator = row.cellIterator();

                PriceDto price = new PriceDto(null, null, null, null, null, null, null, null, null, null, null, null, null);

                while (cellIterator.hasNext()) {
                    cellNumber++;
                    // Excel icerisindeki hucreyi temsil eden nesne
                    Cell cell = (Cell) cellIterator.next();


                    switch (cellNumber) {
                        case 1:
                            switch (cell.getCellType()) {
                                case STRING:
                                    price.setDrugBarcode(Long.valueOf(cell.getStringCellValue().trim()));
                                    break;

                                case BLANK:
                                    //System.out.println("ilaç Barkod Alanı Boş");
                                    break;

                                case NUMERIC:
                                    price.setDrugBarcode(Long.valueOf((long) cell.getNumericCellValue()));
                                    break;
                            }

                            break;
                        case 21:
                            switch (cell.getCellType()) {
                                case STRING:
                                    price.setRealSourcePrice(Double.valueOf((cell.getStringCellValue().trim()).replace(',', '.')));
                                    break;

                                case BLANK:
                                    // System.out.println("Gerçek Kaynak Fiyat (€) Boş");
                                    break;

                                case NUMERIC:
                                    price.setRealSourcePrice(Double.valueOf(cell.getNumericCellValue()));
                                    break;

                                case FORMULA:
                                    price.setRealSourcePrice(Double.valueOf(cell.getNumericCellValue()));
                                    break;
                            }

                            break;
                        case 22:
                            switch (cell.getCellType()) {
                                case STRING:
                                    price.setRealSourcePriceForCalculation(Double.valueOf((cell.getStringCellValue().trim()).replace(',', '.')));
                                    break;

                                case BLANK:
                                    // System.out.println("Hesaplama İçin Kullanılan Gerçek Kaynak Fiyat (€) Boş");
                                    break;

                                case NUMERIC:
                                    price.setRealSourcePriceForCalculation(Double.valueOf(cell.getNumericCellValue()));
                                    break;

                                case FORMULA:
                                    price.setRealSourcePriceForCalculation(Double.valueOf(cell.getNumericCellValue()));
                                    break;
                            }

                            break;
                        case 23:
                            switch (cell.getCellType()) {
                                case STRING:
                                    price.setSourcePrice(Double.valueOf((cell.getStringCellValue().trim()).replace(',', '.')));
                                    break;

                                case BLANK:
                                    // System.out.println(" Kaynak Fiyat (€) Boş");
                                    break;

                                case NUMERIC:
                                    price.setSourcePrice(Double.valueOf(cell.getNumericCellValue()));
                                    break;

                                case FORMULA:
                                    price.setSourcePrice(Double.valueOf(cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 24:
                            switch (cell.getCellType()) {
                                case BLANK:
                                    //System.out.println("Hesaplama İçin Kullanılan Kaynak Ülke");
                                    break;

                                case STRING:
                                    price.setSourceCountryForCalculation(cell.getStringCellValue());
                                    break;
                            }
                            break;
                        case 26:
                            switch (cell.getCellType()) {
                                case STRING:
                                    price.setSalePriceType(Integer.valueOf(cell.getStringCellValue().trim()));
                                    break;

                                case BLANK:
                                    //System.out.println("Satış Fiyat Türü boş");
                                    break;

                                case NUMERIC:
                                    price.setSalePriceType(Integer.valueOf((int) cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 29:
                            switch (cell.getCellType()) {
                                case STRING:
                                    price.setSalePriceToDepotExcludingVat(Double.valueOf((cell.getStringCellValue().trim()).replace(',', '.')));
                                    break;

                                case BLANK:
                                    // System.out.println("KDV Hariç Depocuya Satış TL Fiyatı (₺) boş");
                                    break;

                                case NUMERIC:
                                    price.setSalePriceToDepotExcludingVat(Double.valueOf(cell.getNumericCellValue()));
                                    break;

                                case FORMULA:
                                    price.setSalePriceToDepotExcludingVat(Double.valueOf(cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 33:
                            switch (cell.getCellType()) {
                                case STRING:
                                    price.setDepotSalePriceExcludingVat(Double.valueOf((cell.getStringCellValue().trim()).replace(',', '.')));
                                    break;

                                case BLANK:
                                    // System.out.println("KDV Hariç Depocu Satış TL Fiyatı (₺) boş");
                                    break;

                                case NUMERIC:
                                    price.setDepotSalePriceExcludingVat(Double.valueOf(cell.getNumericCellValue()));
                                    break;

                                case FORMULA:
                                    price.setDepotSalePriceExcludingVat(Double.valueOf(cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 34:
                            switch (cell.getCellType()) {
                                case STRING:
                                    price.setPharmacistSalePriceExcludingVat(Double.valueOf((cell.getStringCellValue().trim()).replace(',', '.')));
                                    break;

                                case BLANK:
                                    // System.out.println("KDV Hariç Eczacı Satış TL Fiyatı (₺) boş");
                                    break;

                                case NUMERIC:
                                    price.setPharmacistSalePriceExcludingVat(Double.valueOf(cell.getNumericCellValue()));
                                    break;

                                case FORMULA:
                                    price.setPharmacistSalePriceExcludingVat(Double.valueOf(cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 35:
                            switch (cell.getCellType()) {
                                case STRING:
                                    price.setRetailSalePriceIncludingVat(Double.valueOf((cell.getStringCellValue().trim()).replace(',', '.')));
                                    break;

                                case BLANK:
                                    // System.out.println("KDV Dahil Perakende Satış TL Fiyatı (₺) boş");
                                    break;

                                case NUMERIC:
                                    price.setRetailSalePriceIncludingVat(Double.valueOf(cell.getNumericCellValue()));
                                    break;

                                case FORMULA:
                                    price.setRetailSalePriceIncludingVat(Double.valueOf(cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 36:
                            switch (cell.getCellType()) {
                                case STRING:
                                    Date dateOfCh = new SimpleDateFormat("dd.MM.yyyy").parse(cell.getStringCellValue());
                                    price.setDateOfChange(dateOfCh);
                                    break;

                                case BLANK:
                                    // System.out.println("Değişiklik Tarihi Boş");
                                    break;

                                case NUMERIC:
                                    Date excelDate = DateUtil.getJavaDate((double) cell.getNumericCellValue());
                                    String stringExcelDate = new SimpleDateFormat("dd.MM.yyyy").format(excelDate);
                                    /* Eski Tarih */
                                    //Date dateOfChNum = new SimpleDateFormat("dd.MM.yyyy").parse(String.valueOf((int) cell.getNumericCellValue()));
                                    price.setDateOfChange(new SimpleDateFormat("dd.MM.yyyy").parse(stringExcelDate));

                                    break;

                            }
                            break;
                        case 37:
                            switch (cell.getCellType()) {
                                case STRING:
                                    Date dateOfVl = new SimpleDateFormat("dd.MM.yyyy").parse(cell.getStringCellValue());
                                    price.setValidityDate(dateOfVl);
                                    break;

                                case BLANK:
                                    //  System.out.println("Geçerlilik Tarihi Boş");
                                    break;

                                case NUMERIC:
                                    Date excelDate = DateUtil.getJavaDate((double) cell.getNumericCellValue());
                                    String stringExcelDate = new SimpleDateFormat("dd.MM.yyyy").format(excelDate);
                                    /* Eski Tarih */
                                    //Date dateOfVlNum = new SimpleDateFormat("dd.MM.yyyy").parse(String.valueOf((int) cell.getNumericCellValue()));
                                    price.setValidityDate(new SimpleDateFormat("dd.MM.yyyy").parse(stringExcelDate));

                                    break;

                            }

                            break;

                    }
                }

                if (price.getDrugBarcode() == null) {
                    /* Satır Boş İse Döngü Sonlansın */
                    break;
                }

                //okunan satır hashmap e eklendi
                priceMap.put(price.getDrugBarcode(), price);
            }

            file.close();
            workbook.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }


        return priceMap;
    }

    public HashMap<Long, PriceDto> readNewVersionExcel(String fileName) {
        HashMap<Long, PriceDto> priceMap = new HashMap<>();

        int cellNumber = 0;

        try {
            //System.out.println("Excel Dosyası Okunuyor...");
            FileInputStream file = new FileInputStream(new File("docs/" + fileName + ".xlsx"));
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            // Excel Dosyasının Hangi Sayfası İle Çalışacağımızı Seçelim.
            XSSFSheet sheet = workbook.getSheetAt(0);
            // Belirledigimiz sayfa icerisinde tum satirlari tek tek dolasacak iterator nesnesi
            Iterator rowIterator = sheet.iterator();
            Row row = (Row) rowIterator.next();       //ilk satır başlık olduğu için geçildi

            // Okunacak Satir Oldugu Surece
            while (rowIterator.hasNext()) {
                cellNumber = 0;//yeni satıra geçince hücre sayısı baştan başlıyor
                // Excel içerisindeki satiri temsil eden nesne
                row = (Row) rowIterator.next();
                // Her bir satir icin tum hucreleri dolasacak iterator nesnesi
                Iterator cellIterator = row.cellIterator();

                PriceDto price = new PriceDto(null, null, null, null, null, null, null, null, null, null, null, null, null);

                while (cellIterator.hasNext()) {
                    cellNumber++;
                    // Excel icerisindeki hucreyi temsil eden nesne
                    Cell cell = (Cell) cellIterator.next();


                    switch (cellNumber) {
                        case 1:
                            switch (cell.getCellType()) {
                                case STRING:
                                    price.setDrugBarcode(Long.valueOf(cell.getStringCellValue().trim()));
                                    break;

                                case BLANK:
                                    //System.out.println("ilaç Barkod Alanı Boş");
                                    break;

                                case NUMERIC:
                                    price.setDrugBarcode(Long.valueOf((long) cell.getNumericCellValue()));
                                    break;
                            }

                            break;
                        case 21:
                            switch (cell.getCellType()) {
                                case STRING:
                                    price.setRealSourcePrice(Double.valueOf((cell.getStringCellValue().trim()).replace(',', '.')));
                                    break;

                                case BLANK:
                                    // System.out.println("Gerçek Kaynak Fiyat (€) Boş");
                                    break;

                                case NUMERIC:
                                    price.setRealSourcePrice(Double.valueOf(cell.getNumericCellValue()));
                                    break;
                            }

                            break;
                        case 22:
                            switch (cell.getCellType()) {
                                case STRING:
                                    price.setRealSourcePriceForCalculation(Double.valueOf((cell.getStringCellValue().trim()).replace(',', '.')));
                                    break;

                                case BLANK:
                                    // System.out.println("Hesaplama İçin Kullanılan Gerçek Kaynak Fiyat (€) Boş");
                                    break;

                                case NUMERIC:
                                    price.setRealSourcePriceForCalculation(Double.valueOf(cell.getNumericCellValue()));
                                    break;
                            }

                            break;
                        case 23:
                            switch (cell.getCellType()) {
                                case STRING:
                                    price.setSourcePrice(Double.valueOf((cell.getStringCellValue().trim()).replace(',', '.')));
                                    break;

                                case BLANK:
                                    // System.out.println(" Kaynak Fiyat (€) Boş");
                                    break;

                                case NUMERIC:
                                    price.setSourcePrice(Double.valueOf(cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 24:
                            switch (cell.getCellType()) {
                                case BLANK:
                                    //System.out.println("Hesaplama İçin Kullanılan Kaynak Ülke");
                                    break;

                                case STRING:
                                    price.setSourceCountryForCalculation(cell.getStringCellValue());
                                    break;
                            }
                            break;
                        case 26:
                            switch (cell.getCellType()) {
                                case STRING:
                                    price.setSalePriceType(Integer.valueOf(cell.getStringCellValue().trim()));
                                    break;

                                case BLANK:
                                    //System.out.println("Satış Fiyat Türü boş");
                                    break;

                                case NUMERIC:
                                    price.setSalePriceType(Integer.valueOf((int) cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 27:
                            switch (cell.getCellType()) {
                                case STRING:
                                    price.setSalePriceToDepotExcludingVat(Double.valueOf((cell.getStringCellValue().trim()).replace(',', '.')));
                                    break;

                                case BLANK:
                                    // System.out.println("KDV Hariç Depocuya Satış TL Fiyatı (₺) boş");
                                    break;

                                case NUMERIC:
                                    price.setSalePriceToDepotExcludingVat(Double.valueOf((int) cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 29:
                            switch (cell.getCellType()) {
                                case STRING:
                                    price.setDepotSalePriceExcludingVat(Double.valueOf((cell.getStringCellValue().trim()).replace(',', '.')));
                                    break;

                                case BLANK:
                                    // System.out.println("KDV Hariç Depocu Satış TL Fiyatı (₺) boş");
                                    break;

                                case NUMERIC:
                                    price.setDepotSalePriceExcludingVat(Double.valueOf((int) cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 31:
                            switch (cell.getCellType()) {
                                case STRING:
                                    price.setPharmacistSalePriceExcludingVat(Double.valueOf((cell.getStringCellValue().trim()).replace(',', '.')));
                                    break;

                                case BLANK:
                                    // System.out.println("KDV Hariç Eczacı Satış TL Fiyatı (₺) boş");
                                    break;

                                case NUMERIC:
                                    price.setPharmacistSalePriceExcludingVat(Double.valueOf((int) cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 32:
                            switch (cell.getCellType()) {
                                case STRING:
                                    price.setRetailSalePriceIncludingVat(Double.valueOf((cell.getStringCellValue().trim()).replace(',', '.')));
                                    break;

                                case BLANK:
                                    // System.out.println("KDV Dahil Perakende Satış TL Fiyatı (₺) boş");
                                    break;

                                case NUMERIC:
                                    price.setRetailSalePriceIncludingVat(Double.valueOf((int) cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 33:
                            switch (cell.getCellType()) {
                                case STRING:
                                    Date dateOfCh = new SimpleDateFormat("dd.MM.yyyy").parse(cell.getStringCellValue());
                                    price.setDateOfChange(dateOfCh);
                                    break;

                                case BLANK:
                                    // System.out.println("Değişiklik Tarihi Boş");
                                    break;

                                case NUMERIC:
                                    Date dateOfChNum = new SimpleDateFormat("dd.MM.yyyy").parse(String.valueOf((int) cell.getNumericCellValue()));
                                    price.setDateOfChange(dateOfChNum);

                                    break;

                            }
                            break;
                        case 34:
                            switch (cell.getCellType()) {
                                case STRING:
                                    Date dateOfVl = new SimpleDateFormat("dd.MM.yyyy").parse(cell.getStringCellValue());
                                    price.setValidityDate(dateOfVl);
                                    break;

                                case BLANK:
                                    //  System.out.println("Geçerlilik Tarihi Boş");
                                    break;

                                case NUMERIC:
                                    Date dateOfVlNum = new SimpleDateFormat("dd.MM.yyyy").parse(String.valueOf((int) cell.getNumericCellValue()));
                                    price.setValidityDate(dateOfVlNum);

                                    break;

                            }

                            break;

                    }
                }

                if (price.getDrugBarcode() == null) {
                    /* Satır Boş İse Döngü Sonlansın */
                    break;
                }

                //okunan satır hashmap e eklendi
                priceMap.put(price.getDrugBarcode(), price);
            }

            file.close();
            workbook.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }


        return priceMap;
    }

    public boolean deleteFromExcelUnnecessaryTabs(String fileName) {
        //Excel Boyut Düşürme Gereksiz Sekmeler Siliniyor
        try {
            FileInputStream file = new FileInputStream(new File("docs/" + fileName + ".xlsx"));
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            if (workbook.getNumberOfSheets() > 1) {
                int totalSheetCount = workbook.getNumberOfSheets();
                for (int i = 1; i < totalSheetCount; i++) {
                    // System.out.println("** " + workbook.getSheetName(1));
                    workbook.removeSheetAt(1);
                }
                FileOutputStream output = new FileOutputStream("docs/" + fileName + ".xlsx");
                workbook.write(output);
                output.close();
            }
            workbook.close();
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean dateControl(Date newDate, Date oldDate) {
        return newDate.equals(oldDate);
    }

    public Price saveOrUpdatePriceFromExcel(PriceDto dto) throws Exception {
        Optional<DrugCard> optDrugCard = drugCardRepository.findByDrugCode(dto.getDrugBarcode());

        if (optDrugCard.isPresent()) {


            Optional<Price> optPrice = priceRepository.findByDrugBarcode(dto.getDrugBarcode());


            if (!optPrice.isPresent()) {
                //fiyat Yeni Kayıt İse Veritabanına Kaydedilir
                Price price1 = mapper.map(dto, Price.class);
                price1.setStatus(1);
                price1.setDrugCard(optDrugCard.get());
                price1 = priceRepository.save(price1);
                return price1;
            } else if (optPrice.isPresent()) {
                //var olan fiyat güncellenir ve veritabanına kaydedilir
                Price price2 = optPrice.get();
                price2.setRealSourcePrice(dto.getRealSourcePrice());
                price2.setRealSourcePriceForCalculation(dto.getRealSourcePriceForCalculation());
                price2.setSourcePrice(dto.getSourcePrice());
                price2.setSourceCountryForCalculation(dto.getSourceCountryForCalculation());
                price2.setSalePriceType(dto.getSalePriceType());
                price2.setSalePriceToDepotExcludingVat(dto.getSalePriceToDepotExcludingVat());
                price2.setDepotSalePriceExcludingVat(dto.getDepotSalePriceExcludingVat());
                price2.setPharmacistSalePriceExcludingVat(dto.getPharmacistSalePriceExcludingVat());
                price2.setRetailSalePriceIncludingVat(dto.getRetailSalePriceIncludingVat());
                price2.setDateOfChange(dto.getDateOfChange());
                price2.setValidityDate(dto.getValidityDate());
                price2.setStatus(1);
                price2 = priceRepository.save(price2);

                return price2;
            }

        }
        return null;
    }
}
