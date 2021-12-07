package com.via.ecza.service;


import com.via.ecza.dto.DiscountDto;
import com.via.ecza.dto.DiscountExcelDto;
import com.via.ecza.dto.DiscountSaveDto;
import com.via.ecza.dto.DiscountSearchDto;
import com.via.ecza.entity.Discount;
import com.via.ecza.entity.DiscountSetting;
import com.via.ecza.entity.DrugCard;
import com.via.ecza.entity.User;
import com.via.ecza.entity.enumClass.Role;
import com.via.ecza.repo.DiscountRepository;
import com.via.ecza.repo.DiscountSettingRepository;
import com.via.ecza.repo.DrugCardRepository;
import javassist.NotFoundException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.*;

@Service
@Transactional
public class DiscountService {

    @Autowired
    private DiscountRepository discountRepository;
    @Autowired
    private DrugCardRepository drugCardRepository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ControlService controlService;

    @Autowired
    private DiscountSettingRepository discountSettingRepository;


    public Boolean save(@Valid DiscountSaveDto dto) throws Exception {
        try {

            Optional<DrugCard> optDrugCard = drugCardRepository.findById(dto.getDrugCardId());
            if (!optDrugCard.isPresent()) {
                throw new NotFoundException("Drug Card Bulunamadı..");
            }

            DecimalFormat decimalFormat = new DecimalFormat(".##");

            Discount discount = mapper.map(dto, Discount.class);
            DrugCard drugCard = optDrugCard.get();
            discount.setDrugCard(drugCard);
            String gDiscount = decimalFormat.format(Float.valueOf(discount.getGeneralDiscount())).replace(',', '.');
            discount.setGeneralDiscount(Float.valueOf(gDiscount));
            String iDiscount = decimalFormat.format(Float.valueOf(discount.getInstutionDiscount())).replace(',', '.');
            discount.setInstutionDiscount(Float.valueOf(iDiscount));
            discount = discountRepository.save(discount);
            return true;
        } catch (Exception e) {
            throw e;
        }
    }

    public List<DiscountDto> getAll() throws NotFoundException {
        List<Discount> list = new ArrayList<>();
        list = discountRepository.findAll();
        if (list.size() < 1) {
            throw new NotFoundException("İskonto Kaydı Bulunamadı..");
        }
        DiscountDto[] array = mapper.map(list, DiscountDto[].class);
        List<DiscountDto> dtos = Arrays.asList(array);
        return dtos;
    }

    public DiscountDto findById(Long id) throws NotFoundException {
        Optional<Discount> optDiscount = discountRepository.findById(id);
        if (!optDiscount.isPresent()) {
            throw new NotFoundException("İskonto Kaydı Bulunamadı..");
        }
        DiscountDto dto = mapper.map(optDiscount.get(), DiscountDto.class);
        return dto;
    }

    public DiscountDto findByDrugCardForDiscount(Long drugCardId) {
        Optional<Discount> optDiscount = discountRepository.findByDrugCard(drugCardId);
        if (!optDiscount.isPresent()) {
            return null;
        }
        DiscountDto dto = mapper.map(optDiscount.get(), DiscountDto.class);
        return dto;
    }

    public Boolean update(String authHeader, Long discountId, @Valid DiscountDto dto) throws NotFoundException {
        if (!discountId.equals(dto.getDiscountId())) {
            return false;
        }

        DecimalFormat decimalFormat = new DecimalFormat(".##");

        Optional<Discount> optDiscount = discountRepository.findById(discountId);
        if (!optDiscount.isPresent()) {
            return false;
        }

        Discount discount = optDiscount.get();
        discount.setDrugCard(discount.getDrugCard());//drugCard Değişmiyor
        String gDiscount = decimalFormat.format(Float.valueOf(dto.getGeneralDiscount())).replace(',', '.');
        discount.setGeneralDiscount(Float.valueOf(gDiscount));
        String iDiscount = decimalFormat.format(Float.valueOf(dto.getInstutionDiscount())).replace(',', '.');
        discount.setInstutionDiscount(Float.valueOf(iDiscount));
        discount.setSurplusDiscount(dto.getSurplusDiscount());
        discount = discountRepository.save(discount);
        return true;
    }

    public Page<DiscountDto> search(DiscountSearchDto dto, Integer pageNo, Integer pageSize, String sortBy) {
        StringBuilder createSqlQuery = new StringBuilder("select * from drug_card dc inner join discount d on dc.drug_card_id =d.drug_card_id and 1=1 ");
        if (dto.getDrugCardId() != null) createSqlQuery.append("and dc.drug_card_id =" + dto.getDrugCardId() + " ");

        if (dto.getDrugCode() != null) createSqlQuery.append("and dc.drug_code = " + dto.getDrugCode() + " ");
        if (dto.getGeneralDiscount() != 0)
            createSqlQuery.append("and d.general_discount = " + dto.getGeneralDiscount() + " ");

        if (dto.getInstutionDiscount() != 0)
            createSqlQuery.append("and d.instution_discount = " + dto.getInstutionDiscount() + " ");

        if (dto.getSurplusDiscount() != null)
            createSqlQuery.append("and d.surplus_discount ILIKE '%" + dto.getSurplusDiscount() + "%' ");

        if (pageNo == 0) {
            createSqlQuery.append("order by dc.drug_name limit " + pageSize + " offset " + pageNo);
        } else {
            createSqlQuery.append("order by dc.drug_name limit " + pageSize + " offset " + (pageSize * pageNo));
        }


        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Discount.class).getResultList();

        DiscountDto[] dtos = mapper.map(list, DiscountDto[].class);
        List<DiscountDto> dtosList = Arrays.asList(dtos);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
        /*int start = Math.min((int) paging.getOffset(), dtosList.size());
        int end = Math.min((start + paging.getPageSize()), dtosList.size());*/

        int start = 0;
        int end = dtosList.size();
        int totalCount = 0;

        //Arama Kriterlerine Göre Toplam Kayıt Sayısı Başlangıç
        StringBuilder createSqlQueryCount = new StringBuilder("select count(*) from drug_card dc inner join discount d on dc.drug_card_id =d.drug_card_id and 1=1 ");
        if (dto.getDrugCardId() != null)
            createSqlQueryCount.append("and dc.drug_card_id =" + dto.getDrugCardId() + " ");

        if (dto.getDrugCode() != null) createSqlQueryCount.append("and dc.drug_code = " + dto.getDrugCode() + " ");
        if (dto.getGeneralDiscount() != 0)
            createSqlQueryCount.append("and d.general_discount = " + dto.getGeneralDiscount() + " ");

        if (dto.getInstutionDiscount() != 0)
            createSqlQueryCount.append("and d.instution_discount = " + dto.getInstutionDiscount() + " ");

        if (dto.getSurplusDiscount() != null)
            createSqlQueryCount.append("and d.surplus_discount ILIKE '%" + dto.getSurplusDiscount() + "%' ");


        List<Object> countList = entityManager.createNativeQuery(createSqlQueryCount.toString()).getResultList();

        for (Object data : countList) {
            totalCount = Integer.valueOf(String.valueOf((BigInteger) data));
        }
        //Arama Kriterlerine Göre Toplam Kayıt Sayısı Son

        Page<DiscountDto> pageList = new PageImpl<>(dtosList.subList(start, end), paging, totalCount);

        return pageList;

    }

    public DiscountDto findByDrugCard(Long drugCard) throws NotFoundException {
        Optional<Discount> optDiscount = discountRepository.findByDrugCard(drugCard);
        if (!optDiscount.isPresent()) {
            throw new NotFoundException("İskonto Kaydı Bulunamadı..");
        }
        DiscountDto dto = mapper.map(optDiscount.get(), DiscountDto.class);
        return dto;
    }

    public Boolean UpdateFromExcel(String authHeader) throws Exception {
        try {
            User user = controlService.getUserFromToken(authHeader);
            if (user.getRole() != Role.ADMIN && user.getRole() != Role.MANAGER) {
                throw new Exception("Bu Alanda Sadece Admin ve Müdür Yetkisi Vardır");
            }

            HashMap<Long, DiscountExcelDto> newMap = readDiscountExcel();

            if (newMap.size() <= 0) {
                throw new Exception("İskonto Excel Okunamadı !");
            }

            newMap.keySet().stream().forEach(x -> {
                try {
                    saveOrUpdateDiscountFromExcel(newMap.get(x));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return true;
    }

    private List<Double> fullInPriceList(List<Double> pricetSetList, Cell cell) {

        String cellValue = cell.getStringCellValue().trim().replace("\n", " ");
        String[] data = cellValue.split(" ");
        for (String value : data) {
            try {
                cellValue = value.replace(",", ".");
                Double priceCellValue = Double.valueOf(cellValue);
                pricetSetList.add(priceCellValue);

            } catch (NumberFormatException e) {
                continue;
            }
        }

        return pricetSetList;
    }

    public HashMap<Long, DiscountExcelDto> readDiscountExcel() throws IOException {
        HashMap<Long, DiscountExcelDto> discountMap = new HashMap<>();


        int cellNumber = 0;
        int control = 0;


        try {
            //System.out.println("Excel Dosyası Okunuyor...");
            FileInputStream file = new FileInputStream(new File("docs/drugDiscount.xlsx"));
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            // Excel Dosyasının Hangi Sayfası İle Çalışacağımızı Seçelim.
            XSSFSheet sheet = workbook.getSheetAt(0);
            // Belirledigimiz sayfa icerisinde tum satirlari tek tek dolasacak iterator nesnesi
            Iterator rowIterator = sheet.iterator();
            Row row = (Row) rowIterator.next();       //ilk 2 satır başlık olduğu için geçildi

            row = (Row) rowIterator.next();

            Iterator iterator1 = row.cellIterator();
            Cell cell1 = (Cell) iterator1.next();
            cell1 = (Cell) iterator1.next();
            cell1 = (Cell) iterator1.next();
            cell1 = (Cell) iterator1.next();
            cell1 = (Cell) iterator1.next();
            cell1 = (Cell) iterator1.next();
            cell1 = (Cell) iterator1.next();
            cell1 = (Cell) iterator1.next();
            cell1 = (Cell) iterator1.next();
            cell1 = (Cell) iterator1.next();
            cell1 = (Cell) iterator1.next();

            List<Double> pricetSetList = new ArrayList<>();

            pricetSetList = fullInPriceList(pricetSetList, cell1);

            cell1 = (Cell) iterator1.next();
            pricetSetList = fullInPriceList(pricetSetList, cell1);

            cell1 = (Cell) iterator1.next();
            pricetSetList = fullInPriceList(pricetSetList, cell1);

            cell1 = (Cell) iterator1.next();
            pricetSetList = fullInPriceList(pricetSetList, cell1);
            pricetSetList.sort(null);

            DiscountSetting discountSetting = new DiscountSetting();
            discountSetting.setPrice0(pricetSetList.get(0));
            discountSetting.setPrice1(pricetSetList.get(1));
            discountSetting.setPrice2(pricetSetList.get(2));
            discountSetting.setPrice3(pricetSetList.get(3));
            discountSetting.setPrice4(pricetSetList.get(4));
            discountSetting.setPrice5(pricetSetList.get(5));
            discountSetting.setCreatedDate(new Date());
            discountSetting = discountSettingRepository.save(discountSetting);


            // Okunacak Satir Oldugu Surece
            while (rowIterator.hasNext()) {

                //Excelde ki Gereksiz Alt kısımlar okunmadan döngüden çıkar
                if (control == 1) {
                    break;
                }


                cellNumber = 0;//yeni satıra geçince hücre sayısı baştan başlıyor
                // Excel içerisindeki satiri temsil eden nesne
                row = (Row) rowIterator.next();
                // Her bir satir icin tum hucreleri dolasacak iterator nesnesi
                Iterator cellIterator = row.cellIterator();

                DiscountExcelDto discount = new DiscountExcelDto();

                while (cellIterator.hasNext()) {
                    cellNumber++;
                    // Excel icerisindeki hucreyi temsil eden nesne
                    Cell cell = (Cell) cellIterator.next();


                    switch (cellNumber) {
                        case 1:
                            switch (cell.getCellType()) {
                                //Excelde Kamu No Alanı Boş İse Okunacak İlaç Kalmamıştır
                                case BLANK:
                                    control = 1;
                                    break;

                            }
                            break;
                        case 2:
                            switch (cell.getCellType()) {
                                case STRING:
                                    discount.setDrugCode(Long.valueOf(cell.getStringCellValue().trim()));
                                    break;

                                case BLANK:
                                    //System.out.println("ilaç Barkod Alanı Boş");
                                    break;

                                case NUMERIC:
                                    discount.setDrugCode(Long.valueOf((long) cell.getNumericCellValue()));
                                    break;
                            }
                            break;
                        case 9:
                            switch (cell.getCellType()) {
                                case STRING:
                                    discount.setPassivationDate("İlaç Pasife Alınmış");
                                    break;

                                case BLANK:

                                    break;

                                case NUMERIC:
                                    discount.setPassivationDate("İlaç Pasife Alınmış");
                                    break;
                            }
                            break;
                        case 11:
                            switch (cell.getCellType()) {
                                case STRING:
                                    /* Bazı İskonto Hücresinde --- % var */
                                    discount.setInstutionDiscount1(0);
                                    break;

                                case BLANK:

                                    break;

                                case NUMERIC:
                                    discount.setInstutionDiscount1(Float.valueOf((float) cell.getNumericCellValue()) * 100);
                                    break;
                            }
                            break;
                        case 12:
                            switch (cell.getCellType()) {
                                case STRING:
                                    /* Bazı İskonto Hücresinde --- % var */
                                    discount.setInstutionDiscount2(0);
                                    break;

                                case BLANK:

                                    break;

                                case NUMERIC:
                                    discount.setInstutionDiscount2(Float.valueOf((float) cell.getNumericCellValue()) * 100);
                                    break;
                            }
                            break;
                        case 13:
                            switch (cell.getCellType()) {
                                case STRING:
                                    /* Bazı İskonto Hücresinde --- % var */
                                    discount.setInstutionDiscount3(0);
                                    break;

                                case BLANK:

                                    break;

                                case NUMERIC:
                                    discount.setInstutionDiscount3(Float.valueOf((float) cell.getNumericCellValue()) * 100);
                                    break;
                            }
                            break;
                        case 14:
                            switch (cell.getCellType()) {
                                case STRING:
                                    /* Bazı İskonto Hücresinde --- % var */
                                    discount.setInstutionDiscount4(0);
                                    break;

                                case BLANK:

                                    break;

                                case NUMERIC:
                                    discount.setInstutionDiscount4(Float.valueOf((float) cell.getNumericCellValue()) * 100);
                                    break;
                            }
                            break;


                        case 15:
                            switch (cell.getCellType()) {
                                case STRING:
                                    /* Bazı İskonto Hücresinde --- % var */
                                    discount.setGeneralDiscount(0);
                                    break;

                                case BLANK:

                                    break;

                                case NUMERIC:
                                    discount.setGeneralDiscount(Float.valueOf((float) cell.getNumericCellValue()) * 100);
                                    break;
                            }
                            break;


                    }
                    //Excelde ki Gereksiz Alt kısımlar okunmadan döngüden çıkar
                    if (control == 1) {
                        break;
                    }

                    //okunan satır hashmap e eklendi
                    discountMap.put(discount.getDrugCode(), discount);
                }
            }

            workbook.close();
            file.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        return discountMap;
    }

    public Discount saveOrUpdateDiscountFromExcel(DiscountExcelDto dto) throws Exception {


        Optional<DrugCard> optDrugCard = drugCardRepository.findByDrugCode(dto.getDrugCode());
        DiscountSetting discountSetting = discountSettingRepository.getLastDiscountSetting().get();
        DecimalFormat decimalFormat = new DecimalFormat(".##");

        if (optDrugCard.isPresent()) {
            /*Fiyat ve İskonto Varsa*/
            if (optDrugCard.get().getPrice() != null && optDrugCard.get().getDiscount() != null) {
                /*Depocuya Satış Fİyatı*/
                Double salePriceToDepotExcludingVat = optDrugCard.get().getPrice().getSalePriceToDepotExcludingVat();
                Discount discount1 = discountRepository.findByDrugCard(optDrugCard.get().getDrugCardId()).get();

                if (salePriceToDepotExcludingVat <= discountSetting.getPrice0()) {
                    String iDiscount4 = decimalFormat.format(Float.valueOf(dto.getInstutionDiscount4())).replace(',', '.');
                    discount1.setInstutionDiscount(Float.valueOf(iDiscount4));
                } else if (salePriceToDepotExcludingVat >= discountSetting.getPrice1() && salePriceToDepotExcludingVat <= discountSetting.getPrice2()) {
                    String iDiscount3 = decimalFormat.format(Float.valueOf(dto.getInstutionDiscount3())).replace(',', '.');
                    discount1.setInstutionDiscount(Float.valueOf(iDiscount3));
                } else if (salePriceToDepotExcludingVat >= discountSetting.getPrice3() && salePriceToDepotExcludingVat <= discountSetting.getPrice4()) {
                    String iDiscount2 = decimalFormat.format(Float.valueOf(dto.getInstutionDiscount2())).replace(',', '.');
                    discount1.setInstutionDiscount(Float.valueOf(iDiscount2));
                } else if (salePriceToDepotExcludingVat >= discountSetting.getPrice5()) {
                    String iDiscount1 = decimalFormat.format(Float.valueOf(dto.getInstutionDiscount1())).replace(',', '.');
                    discount1.setInstutionDiscount(Float.valueOf(iDiscount1));
                }

                String gDiscount = decimalFormat.format(Float.valueOf(dto.getGeneralDiscount())).replace(',', '.');
                discount1.setGeneralDiscount(Float.valueOf(gDiscount));
                discount1 = discountRepository.save(discount1);
                return discount1;

                /*Fiyat varsa İskonto Yoksa*/
            } else if (optDrugCard.get().getPrice() != null && optDrugCard.get().getDiscount() == null) {
                /*Depocuya Satış Fİyatı*/
                Double salePriceToDepotExcludingVat = optDrugCard.get().getPrice().getSalePriceToDepotExcludingVat();
                Discount discount2 = new Discount();

                if (salePriceToDepotExcludingVat <= discountSetting.getPrice0()) {
                    String iDiscount4 = decimalFormat.format(Float.valueOf(dto.getInstutionDiscount4())).replace(',', '.');
                    discount2.setInstutionDiscount(Float.valueOf(iDiscount4));
                } else if (salePriceToDepotExcludingVat >= discountSetting.getPrice1() && salePriceToDepotExcludingVat <= discountSetting.getPrice2()) {
                    String iDiscount3 = decimalFormat.format(Float.valueOf(dto.getInstutionDiscount3())).replace(',', '.');
                    discount2.setInstutionDiscount(Float.valueOf(iDiscount3));
                } else if (salePriceToDepotExcludingVat >= discountSetting.getPrice3() && salePriceToDepotExcludingVat <= discountSetting.getPrice4()) {
                    String iDiscount2 = decimalFormat.format(Float.valueOf(dto.getInstutionDiscount2())).replace(',', '.');
                    discount2.setInstutionDiscount(Float.valueOf(iDiscount2));
                } else if (salePriceToDepotExcludingVat >= discountSetting.getPrice5()) {
                    String iDiscount1 = decimalFormat.format(Float.valueOf(dto.getInstutionDiscount1())).replace(',', '.');
                    discount2.setInstutionDiscount(Float.valueOf(iDiscount1));
                }

                discount2.setDrugCard(optDrugCard.get());
                String gDiscount = decimalFormat.format(Float.valueOf(dto.getGeneralDiscount())).replace(',', '.');
                discount2.setGeneralDiscount(Float.valueOf(gDiscount));
                discount2.setSurplusDiscount("1+0");
                discount2 = discountRepository.save(discount2);
                return discount2;

            }

        }
        return null;
    }

}
