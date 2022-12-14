package com.via.ecza.service;

import com.itextpdf.text.Font;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.via.ecza.dto.*;
import com.via.ecza.entity.*;
import com.via.ecza.entity.enumClass.Role;
import com.via.ecza.repo.*;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Stream;

@Service
@Transactional
public class DepotService {

    @Autowired
    private DepotRepository depotRepository;
    @Autowired
    private DepotStatusRepository depotStatusRepository;
    @Autowired
    private DrugCardRepository drugCardRepository;
    @Autowired
    private CustomerOrderRepository customerOrderRepository;
    @Autowired
    private SmallBoxRepository smallBoxRepository;
    @Autowired
    private BoxRepository boxRepository;
    @Autowired
    private CustomerSupplyOrderRepository customerSupplyOrderRepository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ControlService controlService;

    public Boolean save(@Valid DepotSaveDto dto) throws Exception {
        try {

            Optional<DrugCard> optionalDrugCard = null;
            Optional<CustomerOrder> optionalCustomerOrder = null;
            Optional<CustomerSupplyOrder> optionalCustomerSupplyOrder = null;


            if (dto.getDrugName() != null) {
                optionalDrugCard = drugCardRepository.findByDrugName(dto.getDrugName());
            } else {
                return false;
            }

            if (dto.getCustomerOrderNo() != null) {
                optionalCustomerOrder = customerOrderRepository.findByCustomerOrderNo(dto.getCustomerOrderNo());
            } else {
                return false;
            }

            if (dto.getSupplierOrderNo() != null) {
                optionalCustomerSupplyOrder = customerSupplyOrderRepository.findBySupplyOrderNo(dto.getSupplierOrderNo());
            } else {
                return false;
            }


            Depot depot = mapper.map(dto, Depot.class);
            depot.setDrugCard(optionalDrugCard.get());
            depot.setCustomerOrder(optionalCustomerOrder.get());
            depot.setCustomerSupplyOrder(optionalCustomerSupplyOrder.get());
            depot.setExpirationDate(dto.getStt());
            depot.setLotNo(dto.getLotNo());
            depot.setSerialNumber(dto.getSerialNumber());
            depot.setDrugBarcode(String.valueOf(optionalDrugCard.get().getDrugCode()));
            if (depot.getCustomerOrder().getCustomer().getCustomerId() == 1) {
                //m????terim stok ise
                //ilac??n durumu stok olacak
                depot.setDepotStatus(depotStatusRepository.findById((long) 10).get());
            } else {
                depot.setDepotStatus(depotStatusRepository.findById((long) 1).get());
            }

            depot.setPosition(dto.getPosition());
            depot.setNote(dto.getNote());
            depot.setItsNo(dto.getItsNo());
            depot.setAdmitionDate(new Date());
            depot = depotRepository.save(depot);


            return true;
        } catch (Exception e) {
            throw e;
        }
    }


    public Page<DepotDto> searchInDepot(DepotSearchDto dto, Integer pageNo, Integer pageSize, String sortBy) {

        StringBuilder createSqlQuery = new StringBuilder("select * from depot d " +
                "inner join drug_card dc on d.drug_card_id =dc.drug_card_id " +
                "inner join customer_order co on d.customer_order_id =co.customer_order_id " +
                "inner join customer_supply_order cso on  d.drug_card_id =cso.drug_card_id " +
                "and d.customer_supply_order_id =cso.customer_supply_order_id " +
                "where d.depot_status_id!=10");

        if (dto.getDepotDrugStatus() == null) {
            createSqlQuery.append(" and d.depot_status_id=1 ");
        }

        if (dto.getDrugCardId() != null) createSqlQuery.append("and dc.drug_card_id =  " + dto.getDrugCardId() + " ");

        if (dto.getDrugCode() != null) createSqlQuery.append("and d.drug_barcode = '" + dto.getDrugCode() + "' ");

        if (dto.getStt() != null) {

            String date = new SimpleDateFormat("YYYY/MM/DD").format(dto.getStt());
            createSqlQuery.append("and d.expiration_date >= to_timestamp('" + date.substring(0, 8) + "01" + "', 'YYYY-MM-DD')\\:\\:timestamp without time zone ");

        }
        if (dto.getLotNo() != null) createSqlQuery.append("and  d.lot_no ILIKE '%" + dto.getLotNo() + "%' ");

        if (dto.getSerialNo() != null)
            createSqlQuery.append("and  d.serial_number ILIKE '%" + dto.getSerialNo() + "%' ");

        if (dto.getCustomerOrderNo() != null)
            createSqlQuery.append("and  co.customer_order_no ILIKE '%" + dto.getCustomerOrderNo().trim() + "%' ");

        if (dto.getSupplierOrderNo() != null)
            createSqlQuery.append("and  cso.supply_order_no ILIKE '%" + dto.getSupplierOrderNo().trim() + "%' ");

        if (dto.getDepotDrugStatus() != null)
            createSqlQuery.append("and d.depot_status_id = " + dto.getDepotDrugStatus() + " ");

        if (dto.getSupplierId() != null)
            createSqlQuery.append(" and cso.supplier_id = " + dto.getSupplierId() + " ");

        if (dto.getSortCriteria() != null && dto.getSortDirection() != null) {
            //datatable kullan??ld??ysa
            if (dto.getSortCriteria().trim().equals("drug_name")) {
                //ila?? ad??na g??re s??ralama yap??l??rsa join li tablo oldu??undan alias de??i??tirilmek zorunda
                if (pageNo == 0) {
                    createSqlQuery.append("order by dc." + dto.getSortCriteria() + " " + dto.getSortDirection() + " limit " + pageSize + " offset " + pageNo);
                } else {
                    createSqlQuery.append("order by dc." + dto.getSortCriteria() + " " + dto.getSortDirection() + " limit " + pageSize + " offset " + (pageSize * pageNo));
                }
            } else {
                //di??er se??eneklere g??re s??ralama yap??l??rsa
                if (pageNo == 0) {
                    createSqlQuery.append("order by d." + dto.getSortCriteria() + " " + dto.getSortDirection() + " limit " + pageSize + " offset " + pageNo);
                } else {
                    createSqlQuery.append("order by d." + dto.getSortCriteria() + " " + dto.getSortDirection() + " limit " + pageSize + " offset " + (pageSize * pageNo));
                }
            }
        } else {
            //datatable kullan??lmad??ysa
            if (pageNo == 0) {
                createSqlQuery.append("order by d.depot_id limit " + pageSize + " offset " + pageNo);
            } else {
                createSqlQuery.append("order by d.depot_id limit " + pageSize + " offset " + (pageSize * pageNo));
            }
        }


        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Depot.class).getResultList();
        DepotDto[] dtos = mapper.map(list, DepotDto[].class);
        List<DepotDto> dtosList = Arrays.asList(dtos);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
       /* int start = Math.min((int) paging.getOffset(), dtosList.size());
        int end = Math.min((start + paging.getPageSize()), dtosList.size()); */

        int start = 0;
        int end = dtosList.size();
        int totalCount = 0;

        //Arama Kriterlerine G??re Toplam Kay??t Say??s?? Ba??lang????
        StringBuilder createSqlQueryCount = new StringBuilder("select count(*) from depot d " +
                "inner join drug_card dc on d.drug_card_id =dc.drug_card_id " +
                "inner join customer_order co on d.customer_order_id =co.customer_order_id " +
                "inner join customer_supply_order cso on  d.drug_card_id =cso.drug_card_id " +
                "and d.customer_supply_order_id =cso.customer_supply_order_id " +
                "where d.depot_status_id!=10");

        if (dto.getDepotDrugStatus() == null) {
            createSqlQueryCount.append(" and d.depot_status_id=1");
        }

        if (dto.getDrugCardId() != null)
            createSqlQueryCount.append("and dc.drug_card_id =  " + dto.getDrugCardId() + " ");

        if (dto.getDrugCode() != null) createSqlQueryCount.append("and d.drug_barcode = '" + dto.getDrugCode() + "' ");

        if (dto.getStt() != null) {

            String date = new SimpleDateFormat("YYYY/MM/DD").format(dto.getStt());
            createSqlQueryCount.append("and d.expiration_date >= to_timestamp('" + date.substring(0, 8) + "01" + "', 'YYYY-MM-DD')\\:\\:timestamp without time zone ");

        }
        if (dto.getLotNo() != null) createSqlQueryCount.append("and  d.lot_no ILIKE '%" + dto.getLotNo() + "%' ");

        if (dto.getSerialNo() != null)
            createSqlQueryCount.append("and  d.serial_number ILIKE '%" + dto.getSerialNo() + "%' ");

        if (dto.getCustomerOrderNo() != null)
            createSqlQueryCount.append("and  co.customer_order_no ILIKE '%" + dto.getCustomerOrderNo().trim() + "%' ");

        if (dto.getSupplierOrderNo() != null)
            createSqlQueryCount.append("and  cso.supply_order_no ILIKE '%" + dto.getSupplierOrderNo().trim() + "%' ");

        if (dto.getDepotDrugStatus() != null)
            createSqlQueryCount.append("and d.depot_status_id = " + dto.getDepotDrugStatus() + " ");

        if (dto.getSupplierId() != null)
            createSqlQueryCount.append(" and cso.supplier_id = " + dto.getSupplierId() + " ");

        List<Object> countList = entityManager.createNativeQuery(createSqlQueryCount.toString()).getResultList();

        for (Object data : countList) {
            totalCount = Integer.valueOf(String.valueOf((BigInteger) data));
        }
        //Arama Kriterlerine G??re Toplam Kay??t Say??s?? Son

        Page<DepotDto> pageList = new PageImpl<>(dtosList.subList(start, end), paging, totalCount);

        return pageList;

    }

    public List<DepotDto> depotSearchForShelf(DepotSearchDto dto) {

        StringBuilder createSqlQuery = new StringBuilder("select * from depot d " +
                "inner join customer_supply_order cso on  d.customer_supply_order_id =cso.customer_supply_order_id " +
                "where d.depot_status_id=1 and cso.supply_order_no = '" + dto.getSupplierOrderNo().trim().toUpperCase() + "' ");
        createSqlQuery.append(" order by d.depot_id");

        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Depot.class).getResultList();
        DepotDto[] dtos = mapper.map(list, DepotDto[].class);
        return Arrays.asList(dtos);
    }

    public List<DepotDto> depotDetail(Long depotId) {

        StringBuilder createSqlQuery = new StringBuilder("select * from depot d " +
                "inner join drug_card dc on d.drug_card_id =dc.drug_card_id " +
                "inner join customer_order co on d.customer_order_id =co.customer_order_id " +
                "inner join customer_supply_order cso on  d.drug_card_id =cso.drug_card_id " +
                "and d.customer_supply_order_id =cso.customer_supply_order_id where d.depot_id =" + depotId);


        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Depot.class).getResultList();
        DepotDto[] dtos = mapper.map(list, DepotDto[].class);
        return Arrays.asList(dtos);
    }

    public DepotDto findById(Long id) throws NotFoundException {
        Optional<Depot> optDepot = depotRepository.findById(id);
        if (!optDepot.isPresent()) {
            throw new NotFoundException("Not found Depot");
        }
        DepotDto dto = mapper.map(optDepot.get(), DepotDto.class);
        return dto;
    }

    public Boolean update(Long depotId, @Valid DepotSaveDto dto) throws NotFoundException {


        try {
            Optional<CustomerOrder> optionalCustomerOrder = null;
            Optional<CustomerSupplyOrder> optionalCustomerSupplyOrder = null;

            Optional<DrugCard> optDrugCard = drugCardRepository.findByDrugName(dto.getDrugName());

            if (!optDrugCard.isPresent())
                return false;

            Optional<Depot> optDepot = depotRepository.findById(depotId);
            if (!optDepot.isPresent())
                return false;

            if (dto.getCustomerOrderNo() != null) {
                optionalCustomerOrder = customerOrderRepository.findByCustomerOrderNo(dto.getCustomerOrderNo());
            } else {
                return false;
            }

            if (dto.getSupplierOrderNo() != null) {
                optionalCustomerSupplyOrder = customerSupplyOrderRepository.findBySupplyOrderNo(dto.getSupplierOrderNo());
            } else {
                return false;
            }

            Depot depot = optDepot.get();
            depot.setPosition(dto.getPosition());
            depot.setNote(dto.getNote());
            //depot.setAdmitionDate(new Date());
            // if(depot.getCustomerOrder().getCustomer().getCustomerId()==1){
            //m????terim stok ise
            //ilac??n durumu stok olacak
            //  depot.setDepotStatus(depotStatusRepository.findById((long) 10).get());
            // }else{
            // depot.setDepotStatus(depotStatusRepository.findById((long) 1).get());
            // }

            depot = depotRepository.save(depot);
            return true;
        } catch (Exception e) {
            throw e;
        }

    }

    public Boolean updateDepotShelf(String authHeader, @Valid DepotShelfUpdateDto dto) throws Exception {

        try {


            User user = controlService.getUserFromToken(authHeader);
            if (user.getRole() != Role.ADMIN && user.getRole() != Role.MANAGER && user.getRole() != Role.WAREHOUSEMAN) {
                throw new Exception("Bu Alanda Sadece Admin, M??d??r ve Depocu Yetkisi Vard??r");
            }

            List<DepotShelfCheckedListDto> checkedList = dto.getCheckedList();

            for (DepotShelfCheckedListDto list : checkedList) {
                if (list.getValue() == true) {
                    Depot depot = depotRepository.findById(list.getDepotId()).get();
                    depot.setPosition(dto.getDrugPosition().trim());
                    depot = depotRepository.save(depot);
                }
            }
            return true;

        } catch (Exception e) {
            throw e;
        }

    }

    public Boolean destroyDrug(Long depotId, @Valid DepotDestroyDrugDto dto) throws NotFoundException {


        try {
            Optional<CustomerOrder> optionalCustomerOrder = null;
            Optional<CustomerSupplyOrder> optionalCustomerSupplyOrder = null;

            Optional<DrugCard> optDrugCard = drugCardRepository.findByDrugName(dto.getDrugName());

            if (!optDrugCard.isPresent())
                return false;

            Optional<Depot> optDepot = depotRepository.findById(depotId);
            if (!optDepot.isPresent())
                return false;

            if (dto.getCustomerOrderNo() != null) {
                optionalCustomerOrder = customerOrderRepository.findByCustomerOrderNo(dto.getCustomerOrderNo());
            } else {
                return false;
            }

            if (dto.getSupplierOrderNo() != null) {
                optionalCustomerSupplyOrder = customerSupplyOrderRepository.findBySupplyOrderNo(dto.getSupplierOrderNo());
            } else {
                return false;
            }

            Depot depot = optDepot.get();
            depot.setPosition(dto.getPosition());
            depot.setNote(dto.getNote());
            //depot.setAdmitionDate(new Date());
            depot.setSendingDate(new Date());
            depot.setDepotStatus(depotStatusRepository.findById((long) 3).get());//imha edildi
            depot = depotRepository.save(depot);
            return true;

        } catch (Exception e) {
            throw e;
        }

    }

    public List<DepotStatus> getDepotStatus() {

        return depotStatusRepository.getDepotStatus();
    }

    public List<DepotDto> getDrugListToCso(Long customerSupplyOrderId) {

        List<Depot> depotList = depotRepository.getDrugListOnlyDepotToCso(customerSupplyOrderId);
        DepotDto[] dtos = mapper.map(depotList, DepotDto[].class);
        List<DepotDto> dtosList = Arrays.asList(dtos);
        return dtosList;
    }

    public List<DepotGroupByLotNoDto> getDepotGroupByLotNo() {
        StringBuilder createSqlQuery = new StringBuilder("SELECT d.drug_card_id,d.drug_barcode, d.lot_no, COUNT(d.depot_id) " +
                "FROM depot d " +
                "inner join drug_card dc on d.drug_card_id =dc.drug_card_id " +
                "where d.depot_status_id =1 " +
                "GROUP BY d.lot_no,d.drug_card_id,d.drug_barcode,dc.drug_name " +
                "ORDER BY dc.drug_name asc,d.lot_no asc");

        List<Object[]> list = entityManager.createNativeQuery(createSqlQuery.toString()).getResultList();

        List<DepotGroupByLotNoDto> dtosList = new ArrayList<>();

        for (Object[] data : list) {
            DepotGroupByLotNoDto dtos = new DepotGroupByLotNoDto();
            dtos.setDrugCard(mapper.map(drugCardRepository.findByDrugCardId(Long.valueOf(String.valueOf((BigInteger) data[0]))).get(), DrugCardDto.class));
            dtos.setDrugBarcode((String) data[1]);
            dtos.setLotNo((String) data[2]);
            dtos.setCount(Integer.valueOf(String.valueOf((BigInteger) data[3])));
            dtosList.add(dtos);
        }

        return dtosList;
    }

    public List<DepotGroupByExpDateDto> getDepotGroupByExpDate() {
        StringBuilder createSqlQuery = new StringBuilder("SELECT d.drug_card_id,d.drug_barcode, d.expiration_date, COUNT(d.depot_id) " +
                "FROM depot d " +
                "inner join drug_card dc on d.drug_card_id =dc.drug_card_id " +
                "where d.depot_status_id =1 " +
                "GROUP BY d.expiration_date,d.drug_card_id,d.drug_barcode,dc.drug_name " +
                "ORDER BY dc.drug_name asc,d.expiration_date asc");

        List<Object[]> list = entityManager.createNativeQuery(createSqlQuery.toString()).getResultList();

        List<DepotGroupByExpDateDto> dtosList = new ArrayList<>();

        for (Object[] data : list) {
            DepotGroupByExpDateDto dtos = new DepotGroupByExpDateDto();
            dtos.setDrugCard(mapper.map(drugCardRepository.findByDrugCardId(Long.valueOf(String.valueOf((BigInteger) data[0]))).get(), DrugCardDto.class));
            dtos.setDrugBarcode((String) data[1]);
            dtos.setExpirationDate((Date) data[2]);
            dtos.setCount(Integer.valueOf(String.valueOf((BigInteger) data[3])));
            dtosList.add(dtos);
        }

        return dtosList;
    }


    public int createDepotPdf(String authHeader, DepotSearchDto dto) throws Exception {

        try {
            //ARAMA BA??LANGI??
            StringBuilder createSqlQuery = new StringBuilder("select * from depot d " +
                    "inner join drug_card dc on d.drug_card_id =dc.drug_card_id " +
                    "inner join customer_order co on d.customer_order_id =co.customer_order_id " +
                    "inner join customer_supply_order cso on  d.drug_card_id =cso.drug_card_id " +
                    "and d.customer_supply_order_id =cso.customer_supply_order_id where d.depot_status_id!=10 ");

            if (dto.getDrugCardId() != null)
                createSqlQuery.append("and dc.drug_card_id =  " + dto.getDrugCardId() + " ");

            if (dto.getDrugCode() != null) createSqlQuery.append("and d.drug_barcode = '" + dto.getDrugCode() + "' ");

            if (dto.getStt() != null) {

                String date = new SimpleDateFormat("YYYY/MM/DD").format(dto.getStt());
                createSqlQuery.append("and d.expiration_date >= to_timestamp('" + date.substring(0, 8) + "01" + "', 'YYYY-MM-DD')\\:\\:timestamp without time zone ");

            }
            if (dto.getLotNo() != null) createSqlQuery.append("and  d.lot_no ILIKE '%" + dto.getLotNo() + "%' ");

            if (dto.getSerialNo() != null)
                createSqlQuery.append("and  d.serial_number ILIKE '%" + String.valueOf(dto.getSerialNo()) + "%' ");

            if (dto.getCustomerOrderNo() != null)
                createSqlQuery.append("and  co.customer_order_no ILIKE '%" + dto.getCustomerOrderNo().trim() + "%' ");

            if (dto.getSupplierOrderNo() != null)
                createSqlQuery.append("and  cso.supply_order_no ILIKE '%" + dto.getSupplierOrderNo().trim() + "%' ");

            if (dto.getDepotDrugStatus() != null)
                createSqlQuery.append("and d.depot_status_id = " + dto.getDepotDrugStatus() + " ");

            createSqlQuery.append(" order by d.depot_id");

            List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Depot.class).getResultList();
            DepotDto[] dtos = mapper.map(list, DepotDto[].class);
            List<DepotDto> dtosList = Arrays.asList(dtos);
            //ARAMA SON

            /* Belge sonuna kullan??c?? Id'si eklmek i??in kullan??c?? bilgisi al??n??yor */
            User user = controlService.getUserFromToken(authHeader);

            //PDF BA??LANGI??
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("docs/depo_pdf_" + user.getUsername() + ".pdf"));
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

            //Tablonun Ba??l?????? Girilir
            Paragraph tableHeader = new Paragraph("Depo ??la?? Listesi", catFont);

            tableHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(tableHeader);

            document.add(new Paragraph("\n"));

            //Tablonun S??tun Say??s?? Girilir
            PdfPTable table = new PdfPTable(10);
            table.setWidths(new int[]{2, 6, 4, 3, 3, 4, 3, 6, 5, 5});

            table.setWidthPercentage(100);
            addTableHeader(table);

            //H??crelere Veriler Girilir
            int a = 0;
            for (DepotDto depot : dtosList) {
                a++;
                addRows(table, String.valueOf(a));
                addRows(table, depot.getDrugCard().getDrugName());
                addRows(table, depot.getDrugBarcode());
                addRows(table, depot.getSerialNumber());
                addRows(table, depot.getLotNo());
                addRows(table, dateFormat.format(depot.getExpirationDate()));
                addRows(table, depot.getPosition());
                addRows(table, depot.getDepotStatus().getExplanation());
                addRows(table, depot.getCustomerOrder().getCustomerOrderNo());
                addRows(table, depot.getCustomerSupplyOrder().getSupplyOrderNo());


            }


            document.add(table);
            document.close();
            //PDF SON
            return 1;

        } catch (Exception e) {
            throw new Exception("Depo Pdf Olu??turma ????leminde Hata Olu??tu.", e);
        }


    }

    private void addTableHeader(PdfPTable table) throws IOException, DocumentException {
        BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
        Font catFont = new Font(bf, 10, Font.NORMAL, BaseColor.BLACK);

        //Tablo S??t??n Ba??l??klar?? Girilir
        Stream.of("No", "??la?? Ad??", "Barkod", "Seri No", "Parti No", "SKT", "Raf", "Durum", "Yurtd?????? Sip No", "Sat??n Alma Sip No")
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

    public int createDepotExcel(String authHeader, DepotSearchDto dto) throws Exception {

        try {
            //ARAMA BA??LANGI??
            StringBuilder createSqlQuery = new StringBuilder("select * from depot d " +
                    "inner join drug_card dc on d.drug_card_id =dc.drug_card_id " +
                    "inner join customer_order co on d.customer_order_id =co.customer_order_id " +
                    "inner join customer_supply_order cso on  d.drug_card_id =cso.drug_card_id " +
                    "and d.customer_supply_order_id =cso.customer_supply_order_id where d.depot_status_id!=10 ");

            if (dto.getDrugCardId() != null)
                createSqlQuery.append("and dc.drug_card_id =  " + dto.getDrugCardId() + " ");

            if (dto.getDrugCode() != null) createSqlQuery.append("and d.drug_barcode = '" + dto.getDrugCode() + "' ");

            if (dto.getStt() != null) {

                String date = new SimpleDateFormat("YYYY/MM/DD").format(dto.getStt());
                createSqlQuery.append("and d.expiration_date >= to_timestamp('" + date.substring(0, 8) + "01" + "', 'YYYY-MM-DD')\\:\\:timestamp without time zone ");

            }
            if (dto.getLotNo() != null) createSqlQuery.append("and  d.lot_no ILIKE '%" + dto.getLotNo() + "%' ");

            if (dto.getSerialNo() != null)
                createSqlQuery.append("and  d.serial_number ILIKE '%" + String.valueOf(dto.getSerialNo()) + "%' ");

            if (dto.getCustomerOrderNo() != null)
                createSqlQuery.append("and  co.customer_order_no ILIKE '%" + dto.getCustomerOrderNo().trim() + "%' ");

            if (dto.getSupplierOrderNo() != null)
                createSqlQuery.append("and  cso.supply_order_no ILIKE '%" + dto.getSupplierOrderNo().trim() + "%' ");

            if (dto.getDepotDrugStatus() != null)
                createSqlQuery.append("and d.depot_status_id = " + dto.getDepotDrugStatus() + " ");

            createSqlQuery.append(" order by d.depot_id");

            List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Depot.class).getResultList();
            DepotDto[] dtos = mapper.map(list, DepotDto[].class);
            List<DepotDto> dtosList = Arrays.asList(dtos);
            //ARAMA SON

            /* Belge sonuna kullan??c?? Id'si eklmek i??in kullan??c?? bilgisi al??n??yor */
            User user = controlService.getUserFromToken(authHeader);

            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Sayfa1");

            //STYLE BA??LANGI??
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
            for (DepotDto depot : dtosList) {
                a++;
                b++;
                XSSFRow row = sheet.createRow((short) a);
                row.createCell(0).setCellValue(b);
                row.getCell(0).setCellStyle(csBody);
                //????erik K??sm?? Bu Sat??rdan ??tibaren Obje Bilgileri ??le Doldurulur
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

            }

            FileOutputStream fileOut = new FileOutputStream("docs/depo_excel_" + user.getUsername() + ".xlsx");
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();

            return 1;
        } catch (Exception e) {
            throw new Exception("Depo Excel Olu??turma ????leminde Hata Olu??tu.", e);
        }
    }

    private void addExcelHeader(XSSFWorkbook workbook, XSSFSheet sheet) throws IOException {
        //STYLE BA??LANGI??
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
        //Bu k??s??m Kolon Say??s??na E??it Olmal??
        rowDate.createCell(9).setCellValue(dateFormat.format(new Date()));
        rowDate.getCell(9).setCellStyle(csDate);


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


        //Ba??l??k verilir
        XSSFRow rowHeader = sheet.createRow((short) 4);
        rowHeader.createCell(0).setCellValue("Depo ??la?? Listesi");
        rowHeader.getCell(0).setCellStyle(csHeading);

        sheet.addMergedRegion(new CellRangeAddress(0, 2, 0, 1));
        //son parametre kolon say??s??na e??it olmal??
        sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 9));


        XSSFRow rowhead = sheet.createRow((short) 6);

        //Kolon ??simleri Belirtilir ??htiya?? Olursa Kolonlar Geni??letilir
        rowhead.createCell(0).setCellValue("No");
        rowhead.getCell(0).setCellStyle(csHeader);
        sheet.setColumnWidth(0, 1500);//kolon geni??letildi
        rowhead.createCell(1).setCellValue("??la?? Ad??");
        rowhead.getCell(1).setCellStyle(csHeader);
        sheet.setColumnWidth(1, 10000);
        rowhead.createCell(2).setCellValue("Barkod");
        rowhead.getCell(2).setCellStyle(csHeader);
        sheet.setColumnWidth(2, 4500);
        rowhead.createCell(3).setCellValue("Seri No");
        rowhead.getCell(3).setCellStyle(csHeader);
        sheet.setColumnWidth(3, 4500);
        rowhead.createCell(4).setCellValue("Parti No");
        rowhead.getCell(4).setCellStyle(csHeader);
        sheet.setColumnWidth(4, 4000);
        rowhead.createCell(5).setCellValue("SKT");
        rowhead.getCell(5).setCellStyle(csHeader);
        sheet.setColumnWidth(5, 3500);
        rowhead.createCell(6).setCellValue("Raf");
        rowhead.getCell(6).setCellStyle(csHeader);
        sheet.setColumnWidth(6, 3500);
        rowhead.createCell(7).setCellValue("Durum");
        rowhead.getCell(7).setCellStyle(csHeader);
        sheet.setColumnWidth(7, 9000);
        rowhead.createCell(8).setCellValue("Yurtd?????? S??p No");
        rowhead.getCell(8).setCellStyle(csHeader);
        sheet.setColumnWidth(8, 5000);
        rowhead.createCell(9).setCellValue("Sat??n Alma Sip No");
        rowhead.getCell(9).setCellStyle(csHeader);
        sheet.setColumnWidth(9, 6000);

        //A4 Sayfaya S????d??rma
        sheet.setFitToPage(true);
        PrintSetup ps = sheet.getPrintSetup();
        ps.setFitWidth((short) 1);
        ps.setFitHeight((short) 0);


    }

    public List<DepotGroupByExpDateDto> searchForGroupByExpDate(DepotSearchDto dto) {

        StringBuilder createSqlQuery = new StringBuilder("SELECT d.drug_card_id,d.drug_barcode, d.expiration_date, COUNT(d.depot_id) as depot_count " +
                "FROM depot d " +
                "inner join drug_card dc on d.drug_card_id =dc.drug_card_id " +
                "where d.depot_status_id =1");
        if (dto.getDrugCardId() != null) {
            createSqlQuery.append(" and d.drug_card_id=" + dto.getDrugCardId());
        }
        if (dto.getDrugCode() != null && dto.getDrugCode().trim().length() > 0) {
            createSqlQuery.append(" and d.drug_barcode='" + dto.getDrugCode().trim() + "'");
        }
        if (dto.getStt() != null) {
            String date = new SimpleDateFormat("YYYY/MM/DD").format(dto.getStt());
            createSqlQuery.append(" and d.expiration_date >= to_timestamp('" + date.substring(0, 8) + "01" + "', 'YYYY-MM-DD')\\:\\:timestamp without time zone ");
        }

        createSqlQuery.append(" GROUP BY d.expiration_date,d.drug_card_id,d.drug_barcode,dc.drug_name");

        if(dto.getSortCriteria()!=null && dto.getSortDirection()!=null){
            //datatable varsa
            if(dto.getSortCriteria().trim().equals("drug_name")){
                createSqlQuery.append(" ORDER BY dc."+dto.getSortCriteria().trim()+" "+dto.getSortDirection());
            }else if(dto.getSortCriteria().trim().equals("drug_barcode")){
                createSqlQuery.append(" ORDER BY d."+dto.getSortCriteria().trim()+" "+dto.getSortDirection());
            }else if(dto.getSortCriteria().trim().equals("expiration_date")){
                createSqlQuery.append(" ORDER BY d."+dto.getSortCriteria().trim()+" "+dto.getSortDirection());
            }else if(dto.getSortCriteria().trim().equals("depot_count")){
                createSqlQuery.append(" ORDER BY "+dto.getSortCriteria().trim()+" "+dto.getSortDirection());
            }
        }else{
            //datatable yoksa
            createSqlQuery.append(" ORDER BY dc.drug_name asc,d.expiration_date asc");
        }



        List<Object[]> list = entityManager.createNativeQuery(createSqlQuery.toString()).getResultList();

        List<DepotGroupByExpDateDto> dtosList = new ArrayList<>();

        for (Object[] data : list) {
            DepotGroupByExpDateDto dtos = new DepotGroupByExpDateDto();
            dtos.setDrugCard(mapper.map(drugCardRepository.findByDrugCardId(Long.valueOf(String.valueOf((BigInteger) data[0]))).get(), DrugCardDto.class));
            dtos.setDrugBarcode((String) data[1]);
            dtos.setExpirationDate((Date) data[2]);
            dtos.setCount(Integer.valueOf(String.valueOf((BigInteger) data[3])));
            dtosList.add(dtos);
        }

        return dtosList;
    }

    public List<DepotGroupByExpDateDto> searchForGroupByLotNo(DepotSearchDto dto) {

        StringBuilder createSqlQuery = new StringBuilder("SELECT d.drug_card_id,d.drug_barcode, d.lot_no, COUNT(d.depot_id) as depot_count " +
                "FROM depot d " +
                "inner join drug_card dc on d.drug_card_id =dc.drug_card_id " +
                "where d.depot_status_id =1");

        if (dto.getDrugCardId() != null) {
            createSqlQuery.append(" and d.drug_card_id=" + dto.getDrugCardId());
        }
        if (dto.getDrugCode() != null && dto.getDrugCode().trim().length() > 0) {
            createSqlQuery.append(" and d.drug_barcode='" + dto.getDrugCode().trim() + "'");
        }
        if (dto.getStt() != null) {
            String date = new SimpleDateFormat("YYYY/MM/DD").format(dto.getStt());
            createSqlQuery.append(" and d.expiration_date >= to_timestamp('" + date.substring(0, 8) + "01" + "', 'YYYY-MM-DD')\\:\\:timestamp without time zone ");
        }


        createSqlQuery.append(" GROUP BY d.lot_no,d.drug_card_id,d.drug_barcode,dc.drug_name");

        if(dto.getSortCriteria()!=null && dto.getSortDirection()!=null){
            //datatable varsa
            if(dto.getSortCriteria().trim().equals("drug_name")){
                createSqlQuery.append(" ORDER BY dc."+dto.getSortCriteria().trim()+" "+dto.getSortDirection());
            }else if(dto.getSortCriteria().trim().equals("drug_barcode")){
                createSqlQuery.append(" ORDER BY d."+dto.getSortCriteria().trim()+" "+dto.getSortDirection());
            }else if(dto.getSortCriteria().trim().equals("lot_no")){
                createSqlQuery.append(" ORDER BY d."+dto.getSortCriteria().trim()+" "+dto.getSortDirection());
            }else if(dto.getSortCriteria().trim().equals("depot_count")){
                createSqlQuery.append(" ORDER BY "+dto.getSortCriteria().trim()+" "+dto.getSortDirection());
            }
        }else{
            //datatable yoksa
            createSqlQuery.append(" ORDER BY dc.drug_name asc,d.lot_no asc");
        }



        List<Object[]> list = entityManager.createNativeQuery(createSqlQuery.toString()).getResultList();

        List<DepotGroupByExpDateDto> dtosList = new ArrayList<>();

        for (Object[] data : list) {
            DepotGroupByExpDateDto dtos = new DepotGroupByExpDateDto();
            dtos.setDrugCard(mapper.map(drugCardRepository.findByDrugCardId(Long.valueOf(String.valueOf((BigInteger) data[0]))).get(), DrugCardDto.class));
            dtos.setDrugBarcode((String) data[1]);
            dtos.setLotNo((String) data[2]);
            dtos.setCount(Integer.valueOf(String.valueOf((BigInteger) data[3])));
            dtosList.add(dtos);
        }

        return dtosList;
    }

}

