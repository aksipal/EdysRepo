package com.via.ecza.service;


import com.itextpdf.text.Font;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.via.ecza.dto.DrugBoxPropertiesSaveDto;
import com.via.ecza.dto.DrugCardAndPriceDto;
import com.via.ecza.dto.DrugCardDto;
import com.via.ecza.dto.DrugSaveVatDto;
import com.via.ecza.entity.*;
import com.via.ecza.entity.enumClass.Role;
import com.via.ecza.repo.DiscountRepository;
import com.via.ecza.repo.DrugBoxPropertiesRepository;
import com.via.ecza.repo.DrugCardRepository;
import com.via.ecza.repo.PriceRepository;
import javassist.NotFoundException;
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
import javax.validation.Valid;
import java.io.*;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Stream;

@Service
@Transactional
public class DrugCardService {

    @Autowired
    private DrugCardRepository drugCardRepository;
    @Autowired
    private DiscountRepository discountRepository;
    @Autowired
    private DrugBoxPropertiesRepository drugBoxPropertiesRepository;
    @Autowired
    private PriceRepository priceRepository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ControlService controlService;


    public Boolean save(String authHeader, @Valid DrugCardAndPriceDto dto) throws Exception {
        try {

            User user = controlService.getUserFromToken(authHeader);
            if (user.getRole() != Role.ADMIN && user.getRole() != Role.MANAGER) {
                throw new Exception("Bu Alanda Sadece Admin ve Müdür Yetkisi Vardır");
            }

            Optional<DrugCard> drugCard = drugCardRepository.findByDrugCode(dto.getDrugCode());
            if (drugCard.isPresent()) {
                throw new Exception("Var Olan İlaç Tekrar Eklenemez !");
            }
            DrugCard dc = new DrugCard();
            dc.setDrugName(dto.getDrugName());
            dc.setDrugCode(dto.getDrugCode());
            dc.setDrugCompany(dto.getDrugCompany());
            //dc.setSourceCountry(dto.getSourceCountry());
            dc.setDrugVat(8D);
            dc.setIsActive(true);
            dc.setSourceCountryForCalculation(dto.getSourceCountryForCalculation());
            dc.setDrugVat(dto.getDrugVat());
            //dc.setDrugVat(8D);
            dc.setStatus(2);//Manuel Girişlerde Düzenleme Butonu Aktif Olacak Bu Yüzden Status 2
            dc = drugCardRepository.save(dc);


            //default iskonto eklenir yeni ilaç için
            addDefaultDiscount(dc);

            Price p = new Price();

            DecimalFormat decimalFormat = new DecimalFormat(".##");


            p.setDrugCard(dc);
            p.setDrugBarcode(dc.getDrugCode());
            String DepotSalePriceExcludingVat = decimalFormat.format(Double.valueOf(dto.getDepotSalePriceExcludingVat())).replace(',', '.');
            p.setDepotSalePriceExcludingVat(Double.valueOf(DepotSalePriceExcludingVat));
            p.setStatus(1);

            if (dto.getSalePriceToDepotExcludingVat() != 0) {
                String SalePriceToDepotExcludingVat = decimalFormat.format(Double.valueOf(dto.getSalePriceToDepotExcludingVat())).replace(',', '.');
                p.setSalePriceToDepotExcludingVat(Double.valueOf(SalePriceToDepotExcludingVat));
            }
//            if(dto.getPharmacistSalePriceExcludingVat()!=0){
//                String PharmacistSalePriceExcludingVat=decimalFormat.format(Double.valueOf(dto.getPharmacistSalePriceExcludingVat())).replace(',','.');
//                p.setPharmacistSalePriceExcludingVat(Double.valueOf(PharmacistSalePriceExcludingVat));
//            }
//            if(dto.getRetailSalePriceIncludingVat()!=0){
//                String RetailSalePriceIncludingVat=decimalFormat.format(Double.valueOf(dto.getRetailSalePriceIncludingVat())).replace(',','.');
//                p.setRetailSalePriceIncludingVat(Double.valueOf(RetailSalePriceIncludingVat));
//            }
            p = priceRepository.save(p);

            return true;
        } catch (Exception e) {
            throw e;
        }
    }

    public List<DrugCardDto> getAll() throws NotFoundException {
        List<DrugCard> list = new ArrayList<>();
        list = drugCardRepository.findAll();
        if (list.size() < 1) {
            throw new NotFoundException("İlaç Kaydı Bulunamadı..");
        }
        DrugCardDto[] array = mapper.map(list, DrugCardDto[].class);
        List<DrugCardDto> dtos = Arrays.asList(array);
        return dtos;
    }

    public DrugCardDto findById(Long id) throws NotFoundException {
        Optional<DrugCard> optDrugCard = drugCardRepository.findById(id);
        if (!optDrugCard.isPresent()) {
            throw new NotFoundException("Not found Drug Card");
        }
        DrugCardDto dto = mapper.map(optDrugCard.get(), DrugCardDto.class);
        return dto;
    }

    public DrugCardAndPriceDto findByIdManual(Long id) throws NotFoundException {
        Optional<DrugCard> optDrugCard = drugCardRepository.findById(id);
        if (optDrugCard.get().getStatus() != 2) {//İlaç Manuel Eklenmediyse
            throw new NotFoundException("İlaç Kaydı Bulunamadı..");
        }
        if (!optDrugCard.isPresent()) {
            throw new NotFoundException("İlaç Kaydı Bulunamadı..");
        }
        DrugCardAndPriceDto dto = mapper.map(optDrugCard.get(), DrugCardAndPriceDto.class);
        dto.setSalePriceToDepotExcludingVat(optDrugCard.get().getPrice().getSalePriceToDepotExcludingVat().floatValue());
        dto.setDepotSalePriceExcludingVat(optDrugCard.get().getPrice().getDepotSalePriceExcludingVat().floatValue());
//        dto.setPharmacistSalePriceExcludingVat(optDrugCard.get().getPrice().getPharmacistSalePriceExcludingVat().floatValue());
//        dto.setRetailSalePriceIncludingVat(optDrugCard.get().getPrice().getRetailSalePriceIncludingVat().floatValue());

        return dto;
    }

    public Boolean update(String authHeader, Long drugCardId, @Valid DrugCardAndPriceDto dto) throws Exception {


        try {
            Optional<DrugCard> drugCard = drugCardRepository.findById(drugCardId);
            if (!drugCard.isPresent()) {
                throw new Exception("İlaç Kaydı Bulunamadı !");
            }
            DrugCard dc = new DrugCard();
            dc.setDrugCardId(drugCardId);
            dc.setDrugName(dto.getDrugName());
            dc.setDrugCode(dto.getDrugCode());
            dc.setDrugCompany(dto.getDrugCompany());
            dc.setSourceCountryForCalculation(dto.getSourceCountryForCalculation());
            dc.setDrugVat(dto.getDrugVat());
            dc.setIsActive(true);
            dc.setStatus(2);//Manuel Girişlerde Düzenleme Butonu Aktif Olacak Bu Yüzden Status 2
            dc = drugCardRepository.save(dc);

            Optional<Price> price = priceRepository.findByDrugBarcode(dto.getDrugCode());
            if (!price.isPresent()) {
                throw new Exception("İlacın Fiyat Bilgisi Bulunamadı !");
            }
            Price p = new Price();

            DecimalFormat decimalFormat = new DecimalFormat(".##");

            p.setPriceId(price.get().getPriceId());
            p.setDrugCard(dc);
            p.setDrugBarcode(dc.getDrugCode());
            String DepotSalePriceExcludingVat = decimalFormat.format(Double.valueOf(dto.getDepotSalePriceExcludingVat())).replace(',', '.');
            p.setDepotSalePriceExcludingVat(Double.valueOf(DepotSalePriceExcludingVat));
            p.setStatus(1);


            if (dto.getSalePriceToDepotExcludingVat() != 0) {
                String SalePriceToDepotExcludingVat = decimalFormat.format(Double.valueOf(dto.getSalePriceToDepotExcludingVat())).replace(',', '.');
                p.setSalePriceToDepotExcludingVat(Double.valueOf(SalePriceToDepotExcludingVat));
            }
//            if(dto.getPharmacistSalePriceExcludingVat()!=0){
//                String PharmacistSalePriceExcludingVat=decimalFormat.format(Double.valueOf(dto.getPharmacistSalePriceExcludingVat())).replace(',','.');
//                p.setPharmacistSalePriceExcludingVat(Double.valueOf(PharmacistSalePriceExcludingVat));
//            }
//            if(dto.getRetailSalePriceIncludingVat()!=0){
//                String RetailSalePriceIncludingVat=decimalFormat.format(Double.valueOf(dto.getRetailSalePriceIncludingVat())).replace(',','.');
//                p.setRetailSalePriceIncludingVat(Double.valueOf(RetailSalePriceIncludingVat));
//            }
            p = priceRepository.save(p);

            return true;
        } catch (Exception e) {
            throw e;
        }

    }

    public Page<DrugCardDto> search(DrugCardDto dto, Integer pageNo, Integer pageSize, String sortBy) {
        StringBuilder createSqlQuery = new StringBuilder("select * from drug_card where 1=1 ");
        if (dto.getDrugCardId() != null) createSqlQuery.append("and drug_card_id =  " + dto.getDrugCardId() + " ");

        if (dto.getDrugCode() != null) createSqlQuery.append("and drug_code = " + dto.getDrugCode() + " ");

        if (dto.getAtcCode() != null) createSqlQuery.append("and atc_code = '" + dto.getAtcCode() + "' ");

        if (dto.getAtcName() != null) createSqlQuery.append("and  atc_name ILIKE '%" + dto.getAtcName().trim() + "%' ");

        if (dto.getDrugCompany() != null)
            createSqlQuery.append("and  drug_company ILIKE  '%" + dto.getDrugCompany().trim() + "%' ");

        if (dto.getActiveMatter() != null)
            createSqlQuery.append("and  active_matter ILIKE  '%" + dto.getActiveMatter().trim() + "%' ");

        if (pageNo == 0) {
            createSqlQuery.append("order by drug_name limit " + pageSize + " offset " + pageNo);
        } else {
            createSqlQuery.append("order by drug_name limit " + pageSize + " offset " + (pageSize * pageNo));
        }

        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), DrugCard.class).getResultList();

        DrugCardDto[] dtos = mapper.map(list, DrugCardDto[].class);
        List<DrugCardDto> dtosList = Arrays.asList(dtos);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
        /*int start = Math.min((int) paging.getOffset(), dtosList.size());
        int end = Math.min((start + paging.getPageSize()), dtosList.size());*/

        int start = 0;
        int end = dtosList.size();
        int totalCount = 0;

        //Arama Kriterlerine Göre Toplam Kayıt Sayısı Başlangıç
        StringBuilder createSqlQueryCount = new StringBuilder("select count(*) from drug_card where 1=1 ");
        if (dto.getDrugCardId() != null) createSqlQueryCount.append("and drug_card_id =  " + dto.getDrugCardId() + " ");

        if (dto.getDrugCode() != null) createSqlQueryCount.append("and drug_code = " + dto.getDrugCode() + " ");

        if (dto.getAtcCode() != null) createSqlQueryCount.append("and atc_code = '" + dto.getAtcCode() + "' ");

        if (dto.getAtcName() != null)
            createSqlQueryCount.append("and  atc_name ILIKE '%" + dto.getAtcName().trim() + "%' ");

        if (dto.getDrugCompany() != null)
            createSqlQueryCount.append("and  drug_company ILIKE  '%" + dto.getDrugCompany().trim() + "%' ");

        if (dto.getActiveMatter() != null)
            createSqlQueryCount.append("and  active_matter ILIKE  '%" + dto.getActiveMatter().trim() + "%' ");

        List<Object> countList = entityManager.createNativeQuery(createSqlQueryCount.toString()).getResultList();

        for (Object data : countList) {
            totalCount = Integer.valueOf(String.valueOf((BigInteger) data));
        }
        //Arama Kriterlerine Göre Toplam Kayıt Sayısı Son

        Page<DrugCardDto> pageList = new PageImpl<>(dtosList.subList(start, end), paging, totalCount);

        return pageList;
    }


    public List<DrugCardDto> findBySearching(String drugName) {
        List<DrugCardDto> dtoList = new ArrayList<>();
        if (drugName.length() > 2) {
//            StringBuilder createSqlQuery = new StringBuilder("select * from drug_card where 1=1 and drug_name ILIKE '%" + drugName + "%' ");

            StringBuilder createSqlQuery = new StringBuilder("(select * from drug_card dc2 where 1=1 and dc2.drug_name ilike '" + drugName + "%')" +
                    "union all (select * from drug_card dc1 where 1=1 and dc1.drug_name ilike '%" + drugName + "%'" +
                    "and not exists (select * from drug_card dc2 where 1=1 and dc2.drug_name ilike '" + drugName + "%'" +
                    "and dc2.drug_card_id=dc1.drug_card_id) order by dc1.drug_name asc)  ");


            List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), DrugCard.class).getResultList();
            DrugCardDto[] dtos = mapper.map(list, DrugCardDto[].class);
            dtoList = Arrays.asList(dtos);

            for (DrugCardDto drug : dtoList) {
                if (drug.getIsActive() != null && drug.getIsActive() == false) {
                    drug.setDrugName(drug.getDrugName() + " (PASİF)");
                }
            }
            return dtoList;
        } else {
            return dtoList;
        }
    }

    public int findByBarcode(DrugCardDto dto) {
        Optional<DrugCard> dc = drugCardRepository.findByDrugCode(dto.getDrugCode());
        if (dc.isPresent()) {
            return 1;//Kayıt var
        } else {
            return 0;//Kayıt Yok
        }

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
            HashMap<Long, DrugCardDto> newMap = readNewVersionExcel(fileName);

            //eski excel okundu
            HashMap<Long, DrugCardDto> oldMap = readNewVersionExcel(fileName + "Old");

            if (newMap.size() <= 0 || oldMap.size() <= 0) {
                throw new Exception("Eski veya Yeni Excel Okunamadı !");
            }

            newMap.keySet().stream().forEach(x -> {
                //eski map te key yoksa yeni kayıttır
                if (oldMap.containsKey(x)) {
                    //true dönerse değişiklik yapılmamıştır
                    if (!dateControl(newMap.get(x).getDateOfChange(), oldMap.get(x).getDateOfChange())) {
                        try {
                            saveOrUpdateDrugCardFromExcel(newMap.get(x));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    try {
                        saveOrUpdateDrugCardFromExcel(newMap.get(x));
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
            HashMap<Long, DrugCardDto> newMap = readOldVersionExcel(fileName);

            //eski excel okundu
            HashMap<Long, DrugCardDto> oldMap = readOldVersionExcel(fileName + "Old");

            if (newMap.size() <= 0 || oldMap.size() <= 0) {
                throw new Exception("Eski veya Yeni Excel Okunamadı !");
            }

            newMap.keySet().stream().forEach(x -> {
                //eski map te key yoksa yeni kayıttır
                if (oldMap.containsKey(x)) {
                    //true dönerse değişiklik yapılmamıştır
                    if (!dateControl(newMap.get(x).getDateOfChange(), oldMap.get(x).getDateOfChange())) {
                        try {
                            saveOrUpdateDrugCardFromExcel(newMap.get(x));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    try {
                        saveOrUpdateDrugCardFromExcel(newMap.get(x));
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

    public int createDrugCardPdf(String authHeader, DrugCardDto dto) throws Exception {

        try {
            //ARAMA BAŞLANGIÇ
            StringBuilder createSqlQuery = new StringBuilder("select * from drug_card where 1=1 ");
            if (dto.getDrugCardId() != null) createSqlQuery.append("and drug_card_id =  " + dto.getDrugCardId() + " ");

            if (dto.getDrugCode() != null) createSqlQuery.append("and drug_code = " + dto.getDrugCode() + " ");

            if (dto.getAtcCode() != null) createSqlQuery.append("and atc_code = '" + dto.getAtcCode() + "' ");

            if (dto.getAtcName() != null)
                createSqlQuery.append("and  atc_name ILIKE '%" + dto.getAtcName().trim() + "%' ");

            if (dto.getDrugCompany() != null)
                createSqlQuery.append("and  drug_company ILIKE  '%" + dto.getDrugCompany().trim() + "%' ");

            if (dto.getActiveMatter() != null)
                createSqlQuery.append("and  active_matter ILIKE  '%" + dto.getActiveMatter().trim() + "%' ");

            createSqlQuery.append("order by drug_name");

            List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), DrugCard.class).getResultList();

            DrugCardDto[] dtos = mapper.map(list, DrugCardDto[].class);
            List<DrugCardDto> dtosList = Arrays.asList(dtos);
            //ARAMA SON

            /* Belge sonuna kullanıcı Id'si eklmek için kullanıcı bilgisi alınıyor */
            User user = controlService.getUserFromToken(authHeader);

            //PDF BAŞLANGIÇ
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("docs/ilac_pdf_" + user.getUsername() + ".pdf"));
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
            Font catFont = new Font(bf, 16, Font.NORMAL, BaseColor.BLACK);

            //Tablonun Başlığı Girilir
            Paragraph tableHeader = new Paragraph("İlaç Listesi", catFont);

            tableHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(tableHeader);

            document.add(new Paragraph("\n"));

            //Tablonun Sütun Sayısı Girilir
            PdfPTable table = new PdfPTable(6);
            table.setWidths(new int[]{2, 6, 3, 5, 3, 4});

            table.setWidthPercentage(100);
            addTableHeader(table);

            //Hücrelere Veriler Girilir
            int a = 0;
            for (DrugCardDto drug : dtosList) {
                a++;
                addRows(table, String.valueOf(a));
                addRows(table, drug.getDrugName());
                addRows(table, String.valueOf(drug.getDrugCode()));
                addRows(table, drug.getDrugCompany());
                addRows(table, drug.getSourceCountry());
                addRows(table, drug.getActiveMatter());
            }


            document.add(table);
            document.close();
            //PDF SON
            return 1;

        } catch (Exception e) {
            throw new Exception("İlaç Pdf Oluşturma İşleminde Hata Oluştu.", e);
        }


    }

    private void addTableHeader(PdfPTable table) throws IOException, DocumentException {
        BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
        Font catFont = new Font(bf, 10, Font.NORMAL, BaseColor.BLACK);

        //Tablo Sütün Başlıkları Girilir
        Stream.of("No", "İlaç Adı", "Barkod", "Firma", "Kaynak Ülke", "Etkin Madde")
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
        Font catFont = new Font(bf, 8, Font.NORMAL, BaseColor.BLACK);


        table.addCell(new Phrase(value, catFont));


    }

    public int createDrugCardExcel(String authHeader, DrugCardDto dto) throws Exception {

        try {
            //ARAMA BAŞLANGIÇ
            StringBuilder createSqlQuery = new StringBuilder("select * from drug_card where 1=1 ");
            if (dto.getDrugCardId() != null) createSqlQuery.append("and drug_card_id =  " + dto.getDrugCardId() + " ");

            if (dto.getDrugCode() != null) createSqlQuery.append("and drug_code = " + dto.getDrugCode() + " ");

            if (dto.getAtcCode() != null) createSqlQuery.append("and atc_code = '" + dto.getAtcCode() + "' ");

            if (dto.getAtcName() != null)
                createSqlQuery.append("and  atc_name ILIKE '%" + dto.getAtcName().trim() + "%' ");

            if (dto.getDrugCompany() != null)
                createSqlQuery.append("and  drug_company ILIKE  '%" + dto.getDrugCompany().trim() + "%' ");

            if (dto.getActiveMatter() != null)
                createSqlQuery.append("and  active_matter ILIKE  '%" + dto.getActiveMatter().trim() + "%' ");

            createSqlQuery.append("order by drug_name");

            List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), DrugCard.class).getResultList();

            DrugCardDto[] dtos = mapper.map(list, DrugCardDto[].class);
            List<DrugCardDto> dtosList = Arrays.asList(dtos);
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
            for (DrugCardDto drug : dtosList) {
                a++;
                b++;
                XSSFRow row = sheet.createRow((short) a);
                row.createCell(0).setCellValue(b);
                row.getCell(0).setCellStyle(csBody);
                //İçerik Kısmı Bu Satırdan İtibaren Obje Bilgileri İle Doldurulur
                row.createCell(1).setCellValue(drug.getDrugName());
                row.getCell(1).setCellStyle(csBody);
                row.createCell(2).setCellValue(String.valueOf(drug.getDrugCode()));
                row.getCell(2).setCellStyle(csBody);
                row.createCell(3).setCellValue(drug.getDrugCompany());
                row.getCell(3).setCellStyle(csBody);
                row.createCell(4).setCellValue(drug.getSourceCountry());
                row.getCell(4).setCellStyle(csBody);
                row.createCell(5).setCellValue(drug.getActiveMatter());
                row.getCell(5).setCellStyle(csBody);


            }

            FileOutputStream fileOut = new FileOutputStream("docs/ilac_excel_" + user.getUsername() + ".xlsx");
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();

            return 1;
        } catch (Exception e) {
            throw new Exception("İlaç Excel Oluşturma İşleminde Hata Oluştu.", e);
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
        rowDate.createCell(5).setCellValue(dateFormat.format(new Date()));
        rowDate.getCell(5).setCellStyle(csDate);


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
        rowHeader.createCell(0).setCellValue("İlaç Listesi");
        rowHeader.getCell(0).setCellStyle(csHeading);

        sheet.addMergedRegion(new CellRangeAddress(0, 2, 0, 1));
        //son parametre kolon sayısına eşit olmalı
        sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 5));


        XSSFRow rowhead = sheet.createRow((short) 6);

        //Kolon İsimleri Belirtilir İhtiyaç Olursa Kolonlar Genişletilir
        rowhead.createCell(0).setCellValue("No");
        rowhead.getCell(0).setCellStyle(csHeader);
        sheet.setColumnWidth(0, 1500);//kolon genişletildi
        rowhead.createCell(1).setCellValue("İlaç Adı");
        rowhead.getCell(1).setCellStyle(csHeader);
        sheet.setColumnWidth(1, 10000);
        rowhead.createCell(2).setCellValue("Barkod");
        rowhead.getCell(2).setCellStyle(csHeader);
        sheet.setColumnWidth(2, 4500);
        rowhead.createCell(3).setCellValue("Firma");
        rowhead.getCell(3).setCellStyle(csHeader);
        sheet.setColumnWidth(3, 7500);
        rowhead.createCell(4).setCellValue("Kaynak Ülke");
        rowhead.getCell(4).setCellStyle(csHeader);
        sheet.setColumnWidth(4, 3500);
        rowhead.createCell(5).setCellValue("Etkin Madde");
        rowhead.getCell(5).setCellStyle(csHeader);
        sheet.setColumnWidth(5, 5000);


        //A4 Sayfaya Sığdırma
        sheet.setFitToPage(true);
        PrintSetup ps = sheet.getPrintSetup();
        ps.setFitWidth((short) 1);
        ps.setFitHeight((short) 0);


    }


    public Integer getDrugStatus(String authHeader, DrugCardDto dto) throws Exception {

        Optional<DrugCard> optionalDrugCard = drugCardRepository.findByDrugName(dto.getDrugName());
        if (!optionalDrugCard.isPresent())
            throw new Exception("İlaç Bulunamadı");

        return optionalDrugCard.get().getStatus();
    }

    public Boolean saveBoxProperties(String authHeader, @Valid DrugBoxPropertiesSaveDto dto) throws Exception {

        Optional<DrugCard> optionalDrugCard = drugCardRepository.findById(dto.getDrugCardId());
        if (!optionalDrugCard.isPresent())
            throw new Exception("İlaç Bulunamadı");

        DrugCard drugCard = optionalDrugCard.get();

        DrugBoxProperties drugBoxProperties = new DrugBoxProperties();

        if (drugCard.getDrugBoxProperties() != null)
            drugBoxProperties = drugCard.getDrugBoxProperties();

        if (dto.getDrugBoxWeight() == null && dto.getDrugBoxWidth() == null &&
                dto.getDrugBoxLength() == null && dto.getDrugBoxHeight() == null) {
            return false;
        }

        drugBoxProperties.setDrugCard(drugCard);
        drugBoxProperties.setDrugBoxWeight(dto.getDrugBoxWeight());
        drugBoxProperties.setDrugBoxWidth(dto.getDrugBoxWidth());
        drugBoxProperties.setDrugBoxLength(dto.getDrugBoxLength());
        drugBoxProperties.setDrugBoxHeight(dto.getDrugBoxHeight());
        drugBoxProperties = drugBoxPropertiesRepository.save(drugBoxProperties);

        return true;
    }

    public Boolean saveVat(String authHeader, DrugSaveVatDto dto) throws Exception {

        Optional<DrugCard> optionalDrugCard = drugCardRepository.findById(dto.getDrugCardId());
        if (!optionalDrugCard.isPresent())
            throw new Exception("İlaç Bulunamadı");

        DrugCard drugCard = optionalDrugCard.get();
        drugCard.setDrugVat(dto.getDrugVat());
        drugCard = drugCardRepository.save(drugCard);

        return true;
    }

    public boolean isActiveControl(String drugName) throws Exception {
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
//        String nowDate=simpleDateFormat.format(new Date());
//        drugCardRepository.updateIsActive(drugName,nowDate);

        /* GÜNCEL */
//        List<DrugCard> drugCardList=drugCardRepository.searchActiveDrugCardByDrugName(drugName);
//        if(drugCardList.size()>=2){
//            for (DrugCard item:drugCardList) {
//                //kontrol eklenecek
//            }
//        }
        return true;
    }

    public boolean addDefaultDiscount(DrugCard drugCard) throws Exception {
        Discount discount = new Discount();
        discount.setDrugCard(drugCard);
        discount.setGeneralDiscount(0);
        discount.setInstutionDiscount(0);
        discount.setSurplusDiscount("1+0");
        discount = discountRepository.save(discount);
        return true;
    }

    public HashMap<Long, DrugCardDto> readOldVersionExcel(String fileName) {
        HashMap<Long, DrugCardDto> drugMap = new HashMap<>();

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

                DrugCardDto drug = new DrugCardDto(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

                while (cellIterator.hasNext()) {
                    cellNumber++;
                    // Excel icerisindeki hucreyi temsil eden nesne
                    Cell cell = (Cell) cellIterator.next();


                    switch (cellNumber) {
                        case 1:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setDrugCode(Long.valueOf(cell.getStringCellValue().trim()));
                                    break;

                                case BLANK:
                                    //System.out.println("ilaç Barkod Alanı Boş");
                                    break;

                                case NUMERIC:
                                    drug.setDrugCode(Long.valueOf((long) cell.getNumericCellValue()));
                                    break;
                            }

                            break;
                        case 2:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setAtcCode(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    // System.out.println("Atc Kod Alanı Boş");
                                    break;

                                case NUMERIC:
                                    drug.setAtcCode(String.valueOf(cell.getNumericCellValue()));
                                    break;
                            }

                            break;
                        case 3:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setAtcName(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    // System.out.println("Atc Ad Alanı Boş");
                                    break;

                                case NUMERIC:
                                    drug.setAtcName(String.valueOf(cell.getNumericCellValue()));
                                    break;
                            }

                            break;
                        case 4:
                            switch (cell.getCellType()) {
                                case BLANK:
                                    // System.out.println("Referans değeri boş");
                                    break;

                                case STRING:
                                    drug.setReference(cell.getStringCellValue());
                                    break;
                            }

                            break;
                        case 5:
                            switch (cell.getCellType()) {
                                case BLANK:
                                    //System.out.println("Fiyat korumalı-korumasız değeri boş");
                                    break;

                                case STRING:
                                    drug.setPriceProtectedUnprotected(cell.getStringCellValue());
                                    break;
                            }
                            break;
                        case 6:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setPriceDecReqCode(Integer.valueOf(cell.getStringCellValue().trim()));
                                    break;

                                case BLANK:
                                    //System.out.println("Fiyat Kararnamesi Gereği Kodu boş");
                                    break;

                                case NUMERIC:
                                    drug.setPriceDecReqCode(Integer.valueOf((int) cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 7:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setEquivalentCode(Integer.valueOf(cell.getStringCellValue().trim()));
                                    break;

                                case BLANK:
                                    // System.out.println("Eşdeğeri Kodu boş");
                                    break;

                                case NUMERIC:
                                    drug.setEquivalentCode(Integer.valueOf((int) cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 8:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setReferenceStatusCode(Integer.valueOf(cell.getStringCellValue().trim()));
                                    break;

                                case BLANK:
                                    // System.out.println("Referans Durumu Kodu boş");
                                    break;

                                case NUMERIC:
                                    drug.setReferenceStatusCode(Integer.valueOf((int) cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 12:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setDrugName(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    // System.out.println("İlaç Adı Alanı Boş");
                                    break;

                                case NUMERIC:
                                    drug.setDrugName(String.valueOf(cell.getNumericCellValue()));
                                    break;
                            }

                            break;
                        case 13:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setActiveMatter(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    // System.out.println("Etkin Madde Alanı Boş");
                                    break;

                                case NUMERIC:
                                    drug.setActiveMatter(String.valueOf(cell.getNumericCellValue()));
                                    break;
                            }

                            break;
                        case 14:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setDrugCompany(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    //System.out.println("Firma Alanı Boş");
                                    break;

                                case NUMERIC:
                                    drug.setDrugCompany(String.valueOf(cell.getNumericCellValue()));
                                    break;
                            }

                            break;
                        case 15:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setUnitQuantity(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    // System.out.println("Birim Mİktar Alanı Boş");
                                    break;

                                case NUMERIC:
                                    drug.setUnitQuantity(String.valueOf((int) cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 16:
                            switch (cell.getCellType()) {
                                case FORMULA:
                                    drug.setUnitType(String.valueOf(cell.getCellFormula()));
                                    break;

                                case STRING:
                                    drug.setUnitType(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    //  System.out.println("Birim Cinsi Alanı Boş");
                                    break;

                                case NUMERIC:
                                    drug.setUnitType(String.valueOf((int) cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 17:
                            switch (cell.getCellType()) {
                                case FORMULA:
                                    drug.setPackageQuantitySize(String.valueOf(cell.getCellFormula()));
                                    break;

                                case STRING:
                                    drug.setPackageQuantitySize(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    //  System.out.println("Ambalaj Miktarı Boyutu Boş");
                                    break;

                                case NUMERIC:
                                    drug.setPackageQuantitySize(String.valueOf((int) cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 18:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setRecipeType(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    //  System.out.println("Reçete Türü Boş");
                                    break;

                                case NUMERIC:
                                    drug.setRecipeType(String.valueOf(cell.getNumericCellValue()));
                                    break;
                            }

                            break;
                        case 19:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setImportedManufactured(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    // System.out.println("ithal-imal Boş");
                                    break;

                                case NUMERIC:
                                    drug.setImportedManufactured(String.valueOf(cell.getNumericCellValue()));
                                    break;
                            }

                            break;
                        case 20:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setSourceCountry(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    //  System.out.println("Kaynak Ülke Boş");
                                    break;

                                case NUMERIC:
                                    drug.setSourceCountry(String.valueOf(cell.getNumericCellValue()));
                                    break;
                            }

                            break;
                        case 24:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setSourceCountryForCalculation(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    //  System.out.println("Kaynak Ülke Boş");
                                    break;

                                case NUMERIC:
                                    drug.setSourceCountryForCalculation(String.valueOf(cell.getNumericCellValue()));
                                    break;
                            }

                            break;
                        case 36:
                            switch (cell.getCellType()) {
                                case STRING:
                                    Date dateOfCh = new SimpleDateFormat("dd.MM.yyyy").parse(cell.getStringCellValue());
                                    drug.setDateOfChange(dateOfCh);
                                    break;

                                case BLANK:
                                    // System.out.println("Değişiklik Tarihi Boş");
                                    break;

                                case NUMERIC:
                                    Date excelDate = DateUtil.getJavaDate((double) cell.getNumericCellValue());
                                    String stringExcelDate = new SimpleDateFormat("dd.MM.yyyy").format(excelDate);
                                    /* Eski Date  */
                                    //Date dateOfChNum = new SimpleDateFormat("dd.MM.yyyy").parse(String.valueOf((int) cell.getNumericCellValue()));
                                    drug.setDateOfChange(new SimpleDateFormat("dd.MM.yyyy").parse(stringExcelDate));
                                    break;
                            }
                            break;
                        case 37:
                            switch (cell.getCellType()) {
                                case STRING:
                                    Date dateOfVl = new SimpleDateFormat("dd.MM.yyyy").parse(cell.getStringCellValue());
                                    drug.setValidityDate(dateOfVl);
                                    break;

                                case BLANK:
                                    //  System.out.println("Geçerlilik Tarihi Boş");
                                    break;

                                case NUMERIC:
                                    Date excelDate = DateUtil.getJavaDate((double) cell.getNumericCellValue());
                                    String stringExcelDate = new SimpleDateFormat("dd.MM.yyyy").format(excelDate);
                                    /* Eski Date */
                                    //Date dateOfVlNum = new SimpleDateFormat("dd.MM.yyyy").parse(String.valueOf((int) cell.getNumericCellValue()));
                                    drug.setValidityDate(new SimpleDateFormat("dd.MM.yyyy").parse(stringExcelDate));

                                    break;

                            }

                            break;
                        case 39:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setTypeOfChangeMadeInList(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    // System.out.println("Listede Yapılan Değişiklik Türü Boş");
                                    break;

                                case NUMERIC:
                                    drug.setTypeOfChangeMadeInList(String.valueOf((int) cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 40:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setExplanation(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    //  System.out.println("Açıklama Boş");
                                    break;

                            }
                            break;
                        case 41:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setExplanationOfAllTransactions(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    //  System.out.println("Yapılan Tüm İşlem Açıklamaları Boş");
                                    break;

                            }
                            break;
                        case 42:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setPreDecisionFdkIncreases(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    //   System.out.println("Karar Öncesi FDK Artışları Boş");
                                    break;

                                case NUMERIC:
                                    drug.setPreDecisionFdkIncreases(String.valueOf((int) cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 43:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setAfterDecisionFdkIncreases(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    //   System.out.println("Karar Sonrası FDK Artışları Boş");
                                    break;

                                case NUMERIC:
                                    drug.setAfterDecisionFdkIncreases(String.valueOf((int) cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 44:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setItsInformation(Integer.valueOf(cell.getStringCellValue().trim()));
                                    break;

                                case BLANK:
                                    //    System.out.println("its Bilgi Boş");
                                    break;

                                case NUMERIC:
                                    drug.setItsInformation(Integer.valueOf((int) cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 45:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setCompanyGln(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    //  System.out.println("Firma GLN Boş");
                                    break;

                                case NUMERIC:
                                    drug.setCompanyGln(String.valueOf(cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 46:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setCompanyTaxNo(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    //  System.out.println("Firma Vergi No Boş");
                                    break;

                                case NUMERIC:
                                    drug.setCompanyTaxNo(String.valueOf(cell.getNumericCellValue()));
                                    break;
                            }
                            break;

                    }
                }

                if (drug.getDrugCode() == null) {
                    /* Satır Boş İse Döngü Sonlansın */
                    break;
                }

                //okunan satır hashmap e eklendi
                drugMap.put(drug.getDrugCode(), drug);
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


        return drugMap;
    }

    public HashMap<Long, DrugCardDto> readNewVersionExcel(String fileName) {
        HashMap<Long, DrugCardDto> drugMap = new HashMap<>();

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

                DrugCardDto drug = new DrugCardDto(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

                while (cellIterator.hasNext()) {
                    cellNumber++;
                    // Excel icerisindeki hucreyi temsil eden nesne
                    Cell cell = (Cell) cellIterator.next();


                    switch (cellNumber) {
                        case 1:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setDrugCode(Long.valueOf(cell.getStringCellValue().trim()));
                                    break;

                                case BLANK:
                                    //System.out.println("ilaç Barkod Alanı Boş");
                                    break;

                                case NUMERIC:
                                    drug.setDrugCode(Long.valueOf((long) cell.getNumericCellValue()));
                                    break;
                            }

                            break;
                        case 2:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setAtcCode(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    // System.out.println("Atc Kod Alanı Boş");
                                    break;

                                case NUMERIC:
                                    drug.setAtcCode(String.valueOf(cell.getNumericCellValue()));
                                    break;
                            }

                            break;
                        case 3:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setAtcName(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    // System.out.println("Atc Ad Alanı Boş");
                                    break;

                                case NUMERIC:
                                    drug.setAtcName(String.valueOf(cell.getNumericCellValue()));
                                    break;
                            }

                            break;
                        case 4:
                            switch (cell.getCellType()) {
                                case BLANK:
                                    // System.out.println("Referans değeri boş");
                                    break;

                                case STRING:
                                    drug.setReference(cell.getStringCellValue());
                                    break;
                            }

                            break;
                        case 5:
                            switch (cell.getCellType()) {
                                case BLANK:
                                    //System.out.println("Fiyat korumalı-korumasız değeri boş");
                                    break;

                                case STRING:
                                    drug.setPriceProtectedUnprotected(cell.getStringCellValue());
                                    break;
                            }
                            break;
                        case 6:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setPriceDecReqCode(Integer.valueOf(cell.getStringCellValue().trim()));
                                    break;

                                case BLANK:
                                    //System.out.println("Fiyat Kararnamesi Gereği Kodu boş");
                                    break;

                                case NUMERIC:
                                    drug.setPriceDecReqCode(Integer.valueOf((int) cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 7:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setEquivalentCode(Integer.valueOf(cell.getStringCellValue().trim()));
                                    break;

                                case BLANK:
                                    // System.out.println("Eşdeğeri Kodu boş");
                                    break;

                                case NUMERIC:
                                    drug.setEquivalentCode(Integer.valueOf((int) cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 8:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setReferenceStatusCode(Integer.valueOf(cell.getStringCellValue().trim()));
                                    break;

                                case BLANK:
                                    // System.out.println("Referans Durumu Kodu boş");
                                    break;

                                case NUMERIC:
                                    drug.setReferenceStatusCode(Integer.valueOf((int) cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 12:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setDrugName(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    // System.out.println("İlaç Adı Alanı Boş");
                                    break;

                                case NUMERIC:
                                    drug.setDrugName(String.valueOf(cell.getNumericCellValue()));
                                    break;
                            }

                            break;
                        case 13:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setActiveMatter(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    // System.out.println("Etkin Madde Alanı Boş");
                                    break;

                                case NUMERIC:
                                    drug.setActiveMatter(String.valueOf(cell.getNumericCellValue()));
                                    break;
                            }

                            break;
                        case 14:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setDrugCompany(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    //System.out.println("Firma Alanı Boş");
                                    break;

                                case NUMERIC:
                                    drug.setDrugCompany(String.valueOf(cell.getNumericCellValue()));
                                    break;
                            }

                            break;
                        case 15:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setUnitQuantity(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    // System.out.println("Birim Mİktar Alanı Boş");
                                    break;

                                case NUMERIC:
                                    drug.setUnitQuantity(String.valueOf((int) cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 16:
                            switch (cell.getCellType()) {
                                case FORMULA:
                                    drug.setUnitType(String.valueOf(cell.getCellFormula()));
                                    break;

                                case STRING:
                                    drug.setUnitType(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    //  System.out.println("Birim Cinsi Alanı Boş");
                                    break;

                                case NUMERIC:
                                    drug.setUnitType(String.valueOf((int) cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 17:
                            switch (cell.getCellType()) {
                                case FORMULA:
                                    drug.setPackageQuantitySize(String.valueOf(cell.getCellFormula()));
                                    break;

                                case STRING:
                                    drug.setPackageQuantitySize(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    //  System.out.println("Ambalaj Miktarı Boyutu Boş");
                                    break;

                                case NUMERIC:
                                    drug.setPackageQuantitySize(String.valueOf((int) cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 18:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setRecipeType(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    //  System.out.println("Reçete Türü Boş");
                                    break;

                                case NUMERIC:
                                    drug.setRecipeType(String.valueOf(cell.getNumericCellValue()));
                                    break;
                            }

                            break;
                        case 19:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setImportedManufactured(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    // System.out.println("ithal-imal Boş");
                                    break;

                                case NUMERIC:
                                    drug.setImportedManufactured(String.valueOf(cell.getNumericCellValue()));
                                    break;
                            }

                            break;
                        case 20:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setSourceCountry(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    //  System.out.println("Kaynak Ülke Boş");
                                    break;

                                case NUMERIC:
                                    drug.setSourceCountry(String.valueOf(cell.getNumericCellValue()));
                                    break;
                            }

                            break;
                        case 24:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setSourceCountryForCalculation(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    //  System.out.println("Hesaplama İçin Kullanılan Kaynak Ülke Boş");
                                    break;

                                case NUMERIC:
                                    drug.setSourceCountryForCalculation(String.valueOf(cell.getNumericCellValue()));
                                    break;
                            }

                            break;
                        case 33:
                            switch (cell.getCellType()) {
                                case STRING:
                                    Date dateOfCh = new SimpleDateFormat("dd.MM.yyyy").parse(cell.getStringCellValue());
                                    drug.setDateOfChange(dateOfCh);
                                    break;

                                case BLANK:
                                    // System.out.println("Değişiklik Tarihi Boş");
                                    break;

                                case NUMERIC:
                                    Date dateOfChNum = new SimpleDateFormat("dd.MM.yyyy").parse(String.valueOf((int) cell.getNumericCellValue()));
                                    drug.setDateOfChange(dateOfChNum);

                                    break;
                            }
                            break;
                        case 34:
                            switch (cell.getCellType()) {
                                case STRING:
                                    Date dateOfVl = new SimpleDateFormat("dd.MM.yyyy").parse(cell.getStringCellValue());
                                    drug.setValidityDate(dateOfVl);
                                    break;

                                case BLANK:
                                    //  System.out.println("Geçerlilik Tarihi Boş");
                                    break;

                                case NUMERIC:
                                    Date dateOfVlNum = new SimpleDateFormat("dd.MM.yyyy").parse(String.valueOf((int) cell.getNumericCellValue()));
                                    drug.setValidityDate(dateOfVlNum);

                                    break;

                            }

                            break;
                        case 36:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setTypeOfChangeMadeInList(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    // System.out.println("Listede Yapılan Değişiklik Türü Boş");
                                    break;

                                case NUMERIC:
                                    drug.setTypeOfChangeMadeInList(String.valueOf((int) cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 37:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setExplanation(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    //  System.out.println("Açıklama Boş");
                                    break;

                            }
                            break;
                        case 38:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setExplanationOfAllTransactions(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    //  System.out.println("Yapılan Tüm İşlem Açıklamaları Boş");
                                    break;

                            }
                            break;
                        case 39:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setPreDecisionFdkIncreases(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    //   System.out.println("Karar Öncesi FDK Artışları Boş");
                                    break;

                                case NUMERIC:
                                    drug.setPreDecisionFdkIncreases(String.valueOf((int) cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 40:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setAfterDecisionFdkIncreases(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    //   System.out.println("Karar Sonrası FDK Artışları Boş");
                                    break;

                                case NUMERIC:
                                    drug.setAfterDecisionFdkIncreases(String.valueOf((int) cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 41:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setItsInformation(Integer.valueOf(cell.getStringCellValue().trim()));
                                    break;

                                case BLANK:
                                    //    System.out.println("its Bilgi Boş");
                                    break;

                                case NUMERIC:
                                    drug.setItsInformation(Integer.valueOf((int) cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 42:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setCompanyGln(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    //  System.out.println("Firma GLN Boş");
                                    break;

                                case NUMERIC:
                                    drug.setCompanyGln(String.valueOf(cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 43:
                            switch (cell.getCellType()) {
                                case STRING:
                                    drug.setCompanyTaxNo(cell.getStringCellValue());
                                    break;

                                case BLANK:
                                    //  System.out.println("Firma Vergi No Boş");
                                    break;

                                case NUMERIC:
                                    drug.setCompanyTaxNo(String.valueOf(cell.getNumericCellValue()));
                                    break;
                            }
                            break;

                    }
                }

                if (drug.getDrugCode() == null) {
                    /* Satır Boş İse Döngü Sonlansın */
                    break;
                }

                //okunan satır hashmap e eklendi
                drugMap.put(drug.getDrugCode(), drug);
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


        return drugMap;
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

    public DrugCard saveOrUpdateDrugCardFromExcel(DrugCardDto dto) throws Exception {

        Optional<DrugCard> optDrugCard = drugCardRepository.findByDrugCode(dto.getDrugCode());


        if (!optDrugCard.isPresent()) {
            //İlaç Yeni Kayıt İse Veritabanına Kaydedilir
            dto.setStatus(1);
            DrugCard drugCard = mapper.map(dto, DrugCard.class);
            drugCard.setPrice(null);
            drugCard.setDrugVat(8D);
            drugCard.setIsActive(true);
            drugCard.setDiscount(null);
            drugCard = drugCardRepository.save(drugCard);
            //default iskonto eklenir yeni ilaç için
            addDefaultDiscount(drugCard);
            return drugCard;
        } else if (optDrugCard.isPresent()) {
            //var olan ilaç güncellenir ve veritabanına kaydedilir
            DrugCard drugCard = optDrugCard.get();//veritabanındaki kayıt alındı.
            drugCard.setDrugName(dto.getDrugName());
            drugCard.setAtcCode(dto.getAtcCode());
            drugCard.setDrugVat(8D);
            drugCard.setAtcName(dto.getAtcName());
            drugCard.setDrugCompany(dto.getDrugCompany());
            drugCard.setReference(dto.getReference());
            drugCard.setPriceProtectedUnprotected(dto.getPriceProtectedUnprotected());
            drugCard.setPriceDecReqCode(dto.getPriceDecReqCode());
            drugCard.setEquivalentCode(dto.getEquivalentCode());
            drugCard.setReferenceStatusCode(dto.getReferenceStatusCode());
            drugCard.setActiveMatter(dto.getActiveMatter());
            drugCard.setUnitQuantity(dto.getUnitQuantity());
            drugCard.setUnitType(dto.getUnitType());
            drugCard.setPackageQuantitySize(dto.getPackageQuantitySize());
            drugCard.setRecipeType(dto.getRecipeType());
            drugCard.setImportedManufactured(dto.getImportedManufactured());
            drugCard.setSourceCountry(dto.getSourceCountry());
            drugCard.setDateOfChange(dto.getDateOfChange());
            drugCard.setValidityDate(dto.getValidityDate());
            drugCard.setTypeOfChangeMadeInList(dto.getTypeOfChangeMadeInList());
            drugCard.setExplanation(dto.getExplanation());
            drugCard.setExplanationOfAllTransactions(dto.getExplanationOfAllTransactions());
            drugCard.setPreDecisionFdkIncreases(dto.getPreDecisionFdkIncreases());
            drugCard.setAfterDecisionFdkIncreases(dto.getAfterDecisionFdkIncreases());
            drugCard.setItsInformation(dto.getItsInformation());
            drugCard.setCompanyGln(dto.getCompanyGln());
            drugCard.setCompanyTaxNo(dto.getCompanyTaxNo());
            drugCard.setSourceCountryForCalculation(dto.getSourceCountryForCalculation());
            drugCard.setIsActive(true);
            drugCard.setStatus(1);
            drugCard = drugCardRepository.save(drugCard);

            return drugCard;
        }
        return null;
    }

    public Boolean UpdateFromExcelDrugsisActiveValue(String authHeader, String fileName) throws Exception {

        //SKRS Listesinde Pasif İlaçlar Taranır ve DB'de isActive Alanı False Yapılır

        User user = controlService.getUserFromToken(authHeader);
        if (user.getRole() != Role.ADMIN && user.getRole() != Role.MANAGER) {
            throw new Exception("Bu Alanda Sadece Admin ve Müdür Yetkisi Vardır");
        }


        try {
            // excel okundu
            HashMap<Long, Boolean> newMap = readSKRSExcelForisActiveValue(fileName);

            if (newMap.size() <= 0) {
                throw new Exception("Excel Okunamadı !");
            }

            //tüm ilaçların is_active alanı true olarak güncellendi
            drugCardRepository.updateDrugsofIsActivetoTrue();

            newMap.keySet().stream().forEach(x -> {

            //hashmap te bulunan ilaçların is_active alanları false olarak güncelleniyor
                    try {
                      drugCardRepository.updateIsActivetoFalse(x);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public HashMap<Long, Boolean> readSKRSExcelForisActiveValue(String fileName) {
        HashMap<Long, Boolean> drugMap = new HashMap<>();

        try {
            FileInputStream file = new FileInputStream(new File("docs/" + fileName+".xlsx"));
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            // Excel Dosyasının Hangi Sayfası İle Çalışacağımızı Seçelim.
            XSSFSheet sheet = workbook.getSheetAt(1);

            Iterator rowIterator = sheet.iterator();


            Row row = (Row) rowIterator.next();          //ilk satır başlık olduğu için geçildi
            row = (Row) rowIterator.next();         //ikinci satır başlık olduğu için geçildi
            row = (Row) rowIterator.next();         //üçüncü satır başlık olduğu için geçildi

            while (rowIterator.hasNext()) {


                row = (Row) rowIterator.next();
                Long barcode=null;


                Cell cell = row.getCell(1);
                switch (cell.getCellType()) {
                    case STRING:
                        barcode=(Long.valueOf(cell.getStringCellValue().trim()));
                        break;

                    case BLANK:
                        //System.out.println("ilaç Barkod Alanı Boş");
                        break;

                    case NUMERIC:
                        barcode=(Long.valueOf((long) cell.getNumericCellValue()));
                        break;
                }
                drugMap.put(barcode,false);

            }

            file.close();
            workbook.close();

            return drugMap;


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return drugMap;
    }



}
