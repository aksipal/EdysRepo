package com.via.ecza.service;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.via.ecza.dto.*;
import com.via.ecza.entity.*;
import com.via.ecza.entity.enumClass.Role;
import com.via.ecza.manager.CustomerOrderManager;
import com.via.ecza.repo.*;
import javassist.NotFoundException;
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
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@Transactional
public class StockService {

    @Autowired
    private DepotRepository depotRepository;
    @Autowired
    private DepotStatusRepository depotStatusRepository;
    @Autowired
    private ControlService controlService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private CustomerOrderManager customerOrderService;
    @Autowired
    private CustomerOrderRepository customerOrderRepository;
    @Autowired
    private DrugCardRepository drugCardRepository;
    @Autowired
    private CustomerSupplyOrderRepository customerSupplyOrderRepository;

    SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");

    public Boolean save(@Valid StockSaveDto dto) throws Exception {
        try {

            Optional<Depot> optDepot = depotRepository.findByDrugBarcode(dto.getDrugBarcode());
            if (optDepot.isPresent()) {
                log.info("Stokta ilaç vardır.");
                throw new Exception("Stokta ilaç vardır.");
            }

            Optional<DrugCard> optionalDrugCard = drugCardRepository.findById(dto.getDrugCardId());
            if (!optionalDrugCard.isPresent()) {
                log.info("Böyle bir ilaç vardır.");
                throw new NotFoundException("Böyle bir ilaç vardır.");
            }

            //SIP-202000001
            //SIP-202000002
            Optional<CustomerOrder> optionalCustomerOrder = customerOrderRepository.findByCustomerOrderNo(dto.getCustomerOrderNo());
            if (!optionalCustomerOrder.isPresent()) {
                log.info("Böyle bir sipariş yoktur.");
                throw new Exception("Böyle bir sipariş yoktur.");
            }

            Depot depot = mapper.map(dto, Depot.class);
            Optional<CustomerSupplyOrder> optionalCustomerSupplyOrder=null;
            if(dto.getSupplierOrderNo()!=null) {
                optionalCustomerSupplyOrder = customerSupplyOrderRepository.findBySupplyOrderNo(dto.getSupplierOrderNo());
                if(optionalCustomerSupplyOrder.isPresent())
                    depot.setCustomerSupplyOrder(optionalCustomerSupplyOrder.get());
            }else{
                return false;
            }

            depot.setDrugCard(optionalDrugCard.get());
            depot.setCustomerOrder(optionalCustomerOrder.get());
            depot.setExpirationDate(dto.getExpirationDate());
            depot.setLotNo(dto.getLotNo());
            depot.setSerialNumber(dto.getSerialNumber());
            depot.setDrugBarcode(String.valueOf(optionalDrugCard.get().getDrugCode()));
            if(depot.getCustomerOrder().getCustomer().getCustomerId()==1){
                //müşterim stok ise
                //ilacın durumu stok olacak
                depot.setDepotStatus(depotStatusRepository.findById((long) 10).get());
            }else{
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

    public DepotDto findById(Long depotId) throws NotFoundException {
        Optional<Depot> optionalDepot = depotRepository.findById(depotId);
        if(!optionalDepot.isPresent()) {
            throw new NotFoundException("İlaç bulunamadı.");
        }
        DepotDto dto = mapper.map(optionalDepot.get(),DepotDto.class);
        return dto;
    }

    public List<StockSearchDto> getAll() throws NotFoundException {
        List<Depot> list = new ArrayList<>();
        list = depotRepository.findAll();
        if (list.size() < 1) {
            throw new NotFoundException("Depoda ürün bulunamadı..");
        }
        StockSearchDto[] array = mapper.map(list, StockSearchDto[].class);
        List<StockSearchDto> dto = Arrays.asList(array);
        return dto;
    }

//    public StockSearchDto findByDrugCardForStock(Long drugCardId){
//        Optional<Depot> optDepot = depotRepository.findByDrugCard(drugCardId);
//        if(!optDepot.isPresent()) {
//            return null;
//        }
//        StockSearchDto dto = mapper.map(optDepot.get(),StockSearchDto.class);
//        return dto;
//    }

    //localhost:8500/admin/stock/search-drug?page=0&size=5
    public Page<StockDepotDto> search(StockSearchDto dto, Integer pageNo, Integer pageSize, String sortBy) {
        StringBuilder createSqlQuery = new StringBuilder("select * from depot d " +
                "inner join drug_card dc on d.drug_card_id =dc.drug_card_id " +
                "inner join customer_order co on d.customer_order_id =co.customer_order_id " +
                "inner join customer_supply_order cso on  d.drug_card_id =cso.drug_card_id " +
                "and d.customer_supply_order_id =cso.customer_supply_order_id where d.depot_status_id=10" +
                "and d.status=0 and stock_counting_explanation is null ");

        if(dto.getDrugCardId() != null)		createSqlQuery.append("and dc.drug_card_id =  "+dto.getDrugCardId()+" ");

        if(dto.getDrugCode() != null)		createSqlQuery.append("and d.drug_barcode = '"+dto.getDrugCode()+"' ");
        if (dto.getExpirationDate() != null) {
            String date = new SimpleDateFormat("YYYY/MM/DD").format(dto.getExpirationDate());
            createSqlQuery.append("and d.expiration_date >= to_timestamp('" +date.substring(0,8)+"01" + "', 'YYYY-MM-DD')\\:\\:timestamp without time zone ");
        }

        if(dto.getLotNo() != null)		createSqlQuery.append("and  d.lot_no ILIKE '%"+dto.getLotNo()+"%' ");

        if(dto.getSerialNumber() != null)		createSqlQuery.append("and  d.serial_number ILIKE '%"+dto.getSerialNumber()+"%' ");

        if(dto.getCustomerOrderNo() != null)		createSqlQuery.append("and  co.customer_order_no ILIKE '%"+dto.getCustomerOrderNo().trim()+"%' ");

        if(dto.getSupplierOrderNo() != null)		createSqlQuery.append("and  cso.supply_order_no ILIKE '%"+dto.getSupplierOrderNo().trim()+"%' ");

        if (dto.getSupplierId() != null)
            createSqlQuery.append(" and cso.supplier_id = " + dto.getSupplierId() + " ");


        if (dto.getSortCriteria() != null && dto.getSortDirection() != null) {
            //datatable kullanıldıysa
            if (dto.getSortCriteria().trim().equals("drug_name")) {
                //ilaç adına göre sıralama yapılırsa join li tablo olduğundan alias değiştirilmek zorunda
                if (pageNo == 0) {
                    createSqlQuery.append("order by dc." + dto.getSortCriteria() + " " + dto.getSortDirection() + " limit " + pageSize + " offset " + pageNo);
                } else {
                    createSqlQuery.append("order by dc." + dto.getSortCriteria() + " " + dto.getSortDirection() + " limit " + pageSize + " offset " + (pageSize * pageNo));
                }
            } else {
                //diğer seçeneklere göre sıralama yapılırsa
                if (pageNo == 0) {
                    createSqlQuery.append("order by d." + dto.getSortCriteria() + " " + dto.getSortDirection() + " limit " + pageSize + " offset " + pageNo);
                } else {
                    createSqlQuery.append("order by d." + dto.getSortCriteria() + " " + dto.getSortDirection() + " limit " + pageSize + " offset " + (pageSize * pageNo));
                }
            }
        } else {
            //datatable kullanılmadıysa
            if(pageNo==0){
                createSqlQuery.append(" order by d.depot_id limit "+pageSize+" offset "+pageNo);
            }else{
                createSqlQuery.append(" order by d.depot_id limit "+pageSize+" offset "+(pageSize*pageNo));
            }
        }

        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Depot.class).getResultList();
        StockDepotDto[] dtos = mapper.map(list, StockDepotDto[].class);
        List<StockDepotDto> dtoList = Arrays.asList(dtos);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
        /*int start = Math.min((int)paging.getOffset(), dtoList.size());
        int end = Math.min((start + paging.getPageSize()), dtoList.size());*/

        int start=0;
        int end=dtoList.size();
        int totalCount=0;

        //Arama Kriterlerine Göre Toplam Kayıt Sayısı Başlangıç
        StringBuilder createSqlQueryCount = new StringBuilder("select count(*) from depot d " +
                "inner join drug_card dc on d.drug_card_id =dc.drug_card_id " +
                "inner join customer_order co on d.customer_order_id =co.customer_order_id " +
                "inner join customer_supply_order cso on  d.drug_card_id =cso.drug_card_id " +
                "and d.customer_supply_order_id =cso.customer_supply_order_id where d.depot_status_id=10 " +
                "and d.status=0 and stock_counting_explanation is null ");

        if(dto.getDrugCardId() != null)		createSqlQueryCount.append("and dc.drug_card_id =  "+dto.getDrugCardId()+" ");

        if(dto.getDrugCode() != null)		createSqlQueryCount.append("and d.drug_barcode = '"+dto.getDrugCode()+"' ");
        if (dto.getExpirationDate() != null) {
            String date = new SimpleDateFormat("YYYY/MM/DD").format(dto.getExpirationDate());
            createSqlQuery.append("and d.expiration_date >= to_timestamp('" +date.substring(0,8)+"01" + "', 'YYYY-MM-DD')\\:\\:timestamp without time zone ");
        }

        if(dto.getLotNo() != null)		createSqlQueryCount.append("and  d.lot_no ILIKE '%"+dto.getLotNo()+"%' ");

        if(dto.getSerialNumber() != null)		createSqlQueryCount.append("and  d.serial_number ILIKE '%"+dto.getSerialNumber()+"%' ");

        if(dto.getCustomerOrderNo() != null)		createSqlQueryCount.append("and  co.customer_order_no ILIKE '%"+dto.getCustomerOrderNo().trim()+"%' ");

        if(dto.getSupplierOrderNo() != null)		createSqlQueryCount.append("and  cso.supply_order_no ILIKE '%"+dto.getSupplierOrderNo().trim()+"%' ");

        if (dto.getSupplierId() != null)
            createSqlQueryCount.append(" and cso.supplier_id = " + dto.getSupplierId() + " ");

        List<Object> countList = entityManager.createNativeQuery(createSqlQueryCount.toString()).getResultList();

        for(Object data :countList){
            totalCount= Integer.valueOf(String.valueOf((BigInteger) data));
        }
        //Arama Kriterlerine Göre Toplam Kayıt Sayısı Son

        Page<StockDepotDto> dtoPage = new PageImpl<>(dtoList.subList(start, end), paging, totalCount);

        return dtoPage;
    }
    public List<DepotDto> stockSearchForShelf(DepotSearchDto dto) {

        StringBuilder createSqlQuery = new StringBuilder("select * from depot d " +
                "inner join customer_supply_order cso on  d.customer_supply_order_id =cso.customer_supply_order_id " +
                "where d.depot_status_id=10 and cso.supply_order_no = '" + dto.getSupplierOrderNo().trim().toUpperCase() + "' ");
        createSqlQuery.append(" order by d.depot_id");

        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Depot.class).getResultList();
        DepotDto[] dtos = mapper.map(list, DepotDto[].class);
        return Arrays.asList(dtos);
    }
    public List<DepotDto> stockDetail(Long depotId) {

        StringBuilder createSqlQuery = new StringBuilder("select * from depot d " +
                "inner join drug_card dc on d.drug_card_id =dc.drug_card_id " +
                "inner join customer_order co on d.customer_order_id =co.customer_order_id " +
                "inner join customer_supply_order cso on  d.drug_card_id =cso.drug_card_id " +
                "and d.customer_supply_order_id =cso.customer_supply_order_id where d.depot_id ="+depotId);

        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Depot.class).getResultList();
        DepotDto[] dtos = mapper.map(list, DepotDto[].class);
        return Arrays.asList(dtos);
    }
    public Boolean updateStockShelf(String authHeader,@Valid DepotShelfUpdateDto dto) throws Exception {

        try {

            User user = controlService.getUserFromToken(authHeader);
            if (user.getRole() != Role.ADMIN && user.getRole() != Role.MANAGER && user.getRole() != Role.WAREHOUSEMAN) {
                throw new Exception("Bu Alanda Sadece Admin, Müdür ve Depocu Yetkisi Vardır");
            }

            List<DepotShelfCheckedListDto> checkedList=dto.getCheckedList();

            for (DepotShelfCheckedListDto list:checkedList) {
                if(list.getValue()==true){
                    Depot depot=depotRepository.findById(list.getDepotId()).get();
                    depot.setPosition(dto.getDrugPosition().trim());
                    depot=depotRepository.save(depot);
                }
            }
            return true;

        } catch (Exception e) {
            throw e;
        }

    }
    public Boolean update(Long depotId, @Valid StockSaveDto dto)  throws NotFoundException  {

        try {
            Optional<DrugCard> optDrugCard = drugCardRepository.findById(dto.getDrugCardId());
            if(!optDrugCard.isPresent())
                return false;

            Optional<Depot> optDepot = depotRepository.findById(depotId);
            if(!optDepot.isPresent())
                return false;

            Optional<CustomerOrder> optionalCustomerOrder=null;
            if(dto.getCustomerOrderNo()!=null) {
                optionalCustomerOrder = customerOrderRepository.findByCustomerOrderNo(dto.getCustomerOrderNo());
            }else{
                return false;
            }

            Optional<CustomerSupplyOrder> optionalCustomerSupplyOrder=null;
            if(dto.getSupplierOrderNo()!=null) {
                optionalCustomerSupplyOrder = customerSupplyOrderRepository.findBySupplyOrderNo(dto.getSupplierOrderNo());
            }else{
                return false;
            }

            Depot depot=optDepot.get();
       /* depot.setDrugCard(optDrugCard.get());
        depot.setExpirationDate(dto.getExpirationDate());
        depot.setLotNo(dto.getLotNo());
        depot.setSerialNumber(dto.getSerialNumber());*/
            depot.setPosition(dto.getPosition());
            depot.setNote(dto.getNote());
       /* depot.setItsNo(dto.getItsNo());
        depot.setDrugBarcode(String.valueOf(optDrugCard.get().getDrugCode()));
        depot.setCustomerOrder(optionalCustomerOrder.get());
        depot.setCustomerSupplyOrder(optionalCustomerSupplyOrder.get());
        depot.setAdmitionDate(new Date());*/
            //if(depot.getCustomerOrder().getCustomer().getCustomerId()==1){
            //müşterim stok ise
            //ilacın durumu stok olacak
            // depot.setDepotStatus(depotStatusRepository.findById((long) 10).get());
            // }else{
            //   depot.setDepotStatus(depotStatusRepository.findById((long) 1).get());
            // }
            depot=depotRepository.save(depot);
            return true;
        } catch (Exception e) {
            throw e;
        }
    }


    public List<StockGroupByLotNoDto> getStockGroupByLotNo() {
        StringBuilder createSqlQuery = new StringBuilder("SELECT d.drug_card_id,d.drug_barcode, d.lot_no, COUNT(d.depot_id) " +
                "FROM depot d " +
                "inner join drug_card dc on d.drug_card_id =dc.drug_card_id " +
                "where d.depot_status_id =10 and stock_counting_explanation is null " +
                "GROUP BY d.lot_no,d.drug_card_id,d.drug_barcode,dc.drug_name " +
                "ORDER BY dc.drug_name asc,d.lot_no asc");

        List<Object[]> list = entityManager.createNativeQuery(createSqlQuery.toString()).getResultList();

        List<StockGroupByLotNoDto> dtosList = new ArrayList<>();

        for(Object[] data :list){
            StockGroupByLotNoDto dtos=new StockGroupByLotNoDto();
            dtos.setDrugCard(mapper.map(drugCardRepository.findByDrugCardId(Long.valueOf(String.valueOf((BigInteger) data[0]))).get(),DrugCardDto.class));
            dtos.setDrugBarcode((String)data[1]);
            dtos.setLotNo((String)data[2]);
            dtos.setCount(Integer.valueOf(String.valueOf((BigInteger) data[3])));
            dtosList.add(dtos);
        }

        return dtosList;
    }

    public List<StockGroupByExpDateDto> getStockGroupByExpDate() {
        StringBuilder createSqlQuery = new StringBuilder("SELECT d.drug_card_id,d.drug_barcode, d.expiration_date, COUNT(d.depot_id) " +
                "FROM depot d inner join drug_card dc on d.drug_card_id =dc.drug_card_id " +
                "where d.depot_status_id =10 and stock_counting_explanation is null " +
                "GROUP BY d.expiration_date,d.drug_card_id,d.drug_barcode,dc.drug_name " +
                "ORDER BY dc.drug_name asc,d.expiration_date asc");

        List<Object[]> list = entityManager.createNativeQuery(createSqlQuery.toString()).getResultList();

        List<StockGroupByExpDateDto> dtosList = new ArrayList<>();

        for(Object[] data :list){
            StockGroupByExpDateDto dtos=new StockGroupByExpDateDto();
            dtos.setDrugCard(mapper.map(drugCardRepository.findByDrugCardId(Long.valueOf(String.valueOf((BigInteger) data[0]))).get(),DrugCardDto.class));
            dtos.setDrugBarcode((String)data[1]);
            dtos.setExpirationDate((Date) data[2]);
            dtos.setCount(Integer.valueOf(String.valueOf((BigInteger) data[3])));
            dtosList.add(dtos);
        }

        return dtosList;
    }




    public List<DepotGroupByExpDateDto> searchStockForGroupByExpDate(DepotSearchDto dto) {

        StringBuilder createSqlQuery = new StringBuilder("SELECT d.drug_card_id,d.drug_barcode, d.expiration_date, COUNT(d.depot_id) " +
                "FROM depot d " +
                "inner join drug_card dc on d.drug_card_id =dc.drug_card_id " +
                "where d.depot_status_id =10 and stock_counting_explanation is null");
        if(dto.getDrugCardId()!=null){
            createSqlQuery.append(" and d.drug_card_id="+dto.getDrugCardId());
        }
        if(dto.getDrugCode()!=null && dto.getDrugCode().trim().length()>0){
            createSqlQuery.append(" and d.drug_barcode='"+dto.getDrugCode().trim()+"'");
        }
        if (dto.getStt() != null) {
            String date = new SimpleDateFormat("YYYY/MM/DD").format(dto.getStt());
            createSqlQuery.append(" and d.expiration_date >= to_timestamp('" + date.substring(0, 8) + "01" + "', 'YYYY-MM-DD')\\:\\:timestamp without time zone ");
        }

        createSqlQuery.append(" GROUP BY d.expiration_date,d.drug_card_id,d.drug_barcode,dc.drug_name ORDER BY dc.drug_name asc,d.expiration_date asc");


        List<Object[]> list = entityManager.createNativeQuery(createSqlQuery.toString()).getResultList();

        List<DepotGroupByExpDateDto> dtosList = new ArrayList<>();

        for(Object[] data :list){
            DepotGroupByExpDateDto dtos=new DepotGroupByExpDateDto();
            dtos.setDrugCard(mapper.map(drugCardRepository.findByDrugCardId(Long.valueOf(String.valueOf((BigInteger) data[0]))).get(),DrugCardDto.class));
            dtos.setDrugBarcode((String)data[1]);
            dtos.setExpirationDate((Date) data[2]);
            dtos.setCount(Integer.valueOf(String.valueOf((BigInteger) data[3])));
            dtosList.add(dtos);
        }

        return dtosList;
    }
    public List<DepotGroupByExpDateDto> searchStockForGroupByLotNo(DepotSearchDto dto) {

        StringBuilder createSqlQuery = new StringBuilder("SELECT d.drug_card_id,d.drug_barcode, d.lot_no, COUNT(d.depot_id) " +
                "FROM depot d " +
                "inner join drug_card dc on d.drug_card_id =dc.drug_card_id " +
                "where d.depot_status_id =10 and stock_counting_explanation is null");

        if(dto.getDrugCardId()!=null){
            createSqlQuery.append(" and d.drug_card_id="+dto.getDrugCardId());
        }
        if(dto.getDrugCode()!=null && dto.getDrugCode().trim().length()>0){
            createSqlQuery.append(" and d.drug_barcode='"+dto.getDrugCode().trim()+"'");
        }
        if (dto.getStt() != null) {
            String date = new SimpleDateFormat("YYYY/MM/DD").format(dto.getStt());
            createSqlQuery.append(" and d.expiration_date >= to_timestamp('" + date.substring(0, 8) + "01" + "', 'YYYY-MM-DD')\\:\\:timestamp without time zone ");
        }


        createSqlQuery.append(" GROUP BY d.lot_no,d.drug_card_id,d.drug_barcode,dc.drug_name ORDER BY dc.drug_name asc,d.lot_no asc");

        List<Object[]> list = entityManager.createNativeQuery(createSqlQuery.toString()).getResultList();

        List<DepotGroupByExpDateDto> dtosList = new ArrayList<>();

        for(Object[] data :list){
            DepotGroupByExpDateDto dtos=new DepotGroupByExpDateDto();
            dtos.setDrugCard(mapper.map(drugCardRepository.findByDrugCardId(Long.valueOf(String.valueOf((BigInteger) data[0]))).get(),DrugCardDto.class));
            dtos.setDrugBarcode((String)data[1]);
            dtos.setLotNo((String)data[2]);
            dtos.setCount(Integer.valueOf(String.valueOf((BigInteger) data[3])));
            dtosList.add(dtos);
        }

        return dtosList;
    }




    public int createStockPdf(String authHeader,StockSearchDto dto) throws Exception {

        try {
            //ARAMA BAŞLANGIÇ
            StringBuilder createSqlQuery = new StringBuilder("select * from depot d " +
                    "inner join drug_card dc on d.drug_card_id =dc.drug_card_id " +
                    "inner join customer_order co on d.customer_order_id =co.customer_order_id " +
                    "inner join customer_supply_order cso on  d.drug_card_id =cso.drug_card_id " +
                    "and d.customer_supply_order_id =cso.customer_supply_order_id where d.depot_status_id=10 ");

            if(dto.getDrugCardId() != null)		createSqlQuery.append("and dc.drug_card_id =  "+dto.getDrugCardId()+" ");

            if(dto.getDrugCode() != null)		createSqlQuery.append("and d.drug_barcode = '"+dto.getDrugCode()+"' ");
            if (dto.getExpirationDate() != null) {
                String date = new SimpleDateFormat("YYYY/MM/DD").format(dto.getExpirationDate());
                createSqlQuery.append("and d.expiration_date >= to_timestamp('" +date.substring(0,8)+"01" + "', 'YYYY-MM-DD')\\:\\:timestamp without time zone ");
            }

            if(dto.getLotNo() != null)		createSqlQuery.append("and  d.lot_no ILIKE '%"+dto.getLotNo()+"%' ");

            if(dto.getSerialNumber() != null)		createSqlQuery.append("and  d.serial_number ILIKE '%"+dto.getSerialNumber()+"%' ");

            if(dto.getCustomerOrderNo() != null)		createSqlQuery.append("and  co.customer_order_no ILIKE '%"+dto.getCustomerOrderNo().trim()+"%' ");

            if(dto.getSupplierOrderNo() != null)		createSqlQuery.append("and  cso.supply_order_no ILIKE '%"+dto.getSupplierOrderNo().trim()+"%' ");

            if (dto.getSupplierId() != null)
                createSqlQuery.append(" and cso.supplier_id = " + dto.getSupplierId() + " ");

            createSqlQuery.append(" order by d.depot_id");

            List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Depot.class).getResultList();
            StockDepotDto[] dtos = mapper.map(list, StockDepotDto[].class);
            List<StockDepotDto> dtosList = Arrays.asList(dtos);
            //ARAMA SON

            /* Belge sonuna kullanıcı Id'si eklmek için kullanıcı bilgisi alınıyor */
            User user = controlService.getUserFromToken(authHeader);

            //PDF BAŞLANGIÇ
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("docs/stok_pdf_"+user.getUsername()+".pdf"));
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
            Paragraph tableHeader = new Paragraph("Stok İlaç Listesi", catFont);

            tableHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(tableHeader);

            document.add(new Paragraph("\n"));

            //Tablonun Sütun Sayısı Girilir
            PdfPTable table = new PdfPTable(10);
            table.setWidths(new int[]{2, 6,  4,3, 3, 4, 3, 6, 5, 5});

            table.setWidthPercentage(100);
            addTableHeader(table);

            //Hücrelere Veriler Girilir
            int a = 0;
            for (StockDepotDto stok : dtosList) {
                a++;
                addRows(table, String.valueOf(a));
                addRows(table, stok.getDrugCard().getDrugName());
                addRows(table, stok.getDrugBarcode());
                addRows(table, stok.getSerialNumber());
                addRows(table, stok.getLotNo());
                addRows(table, dateFormat.format(stok.getExpirationDate()));
                addRows(table, stok.getPosition());
                addRows(table, stok.getDepotStatus().getExplanation());
                addRows(table, stok.getCustomerOrder().getCustomerOrderNo());
                addRows(table, stok.getCustomerSupplyOrder().getSupplyOrderNo());



            }


            document.add(table);
            document.close();
            //PDF SON
            return 1;

        }catch (Exception e){
            throw new Exception("Stok Pdf Oluşturma İşleminde Hata Oluştu.",e);
        }


    }
    private void addTableHeader(PdfPTable table) throws IOException, DocumentException {
        BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
        Font catFont = new Font(bf, 10, Font.NORMAL, BaseColor.BLACK);

        //Tablo Sütün Başlıkları Girilir
        Stream.of("No", "İlaç Adı", "Barkod", "Seri No", "Parti No", "SKT", "Raf", "Durum", "Yurtdışı Sip No", "Satın Alma Sip No")
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

    public int createStockExcel(String authHeader,StockSearchDto dto) throws Exception {

        try {
            //ARAMA BAŞLANGIÇ
            StringBuilder createSqlQuery = new StringBuilder("select * from depot d " +
                    "inner join drug_card dc on d.drug_card_id =dc.drug_card_id " +
                    "inner join customer_order co on d.customer_order_id =co.customer_order_id " +
                    "inner join customer_supply_order cso on  d.drug_card_id =cso.drug_card_id " +
                    "and d.customer_supply_order_id =cso.customer_supply_order_id where d.depot_status_id=10 ");

            if(dto.getDrugCardId() != null)		createSqlQuery.append("and dc.drug_card_id =  "+dto.getDrugCardId()+" ");

            if(dto.getDrugCode() != null)		createSqlQuery.append("and d.drug_barcode = '"+dto.getDrugCode()+"' ");
            if (dto.getExpirationDate() != null) {
                String date = new SimpleDateFormat("YYYY/MM/DD").format(dto.getExpirationDate());
                createSqlQuery.append("and d.expiration_date >= to_timestamp('" +date.substring(0,8)+"01" + "', 'YYYY-MM-DD')\\:\\:timestamp without time zone ");
            }

            if(dto.getLotNo() != null)		createSqlQuery.append("and  d.lot_no ILIKE '%"+dto.getLotNo()+"%' ");

            if(dto.getSerialNumber() != null)		createSqlQuery.append("and  d.serial_number ILIKE '%"+dto.getSerialNumber()+"%' ");

            if(dto.getCustomerOrderNo() != null)		createSqlQuery.append("and  co.customer_order_no ILIKE '%"+dto.getCustomerOrderNo().trim()+"%' ");

            if(dto.getSupplierOrderNo() != null)		createSqlQuery.append("and  cso.supply_order_no ILIKE '%"+dto.getSupplierOrderNo().trim()+"%' ");

            if (dto.getSupplierId() != null)
                createSqlQuery.append(" and cso.supplier_id = " + dto.getSupplierId() + " ");

            createSqlQuery.append(" order by d.depot_id");

            List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Depot.class).getResultList();
            StockDepotDto[] dtos = mapper.map(list, StockDepotDto[].class);
            List<StockDepotDto> dtosList = Arrays.asList(dtos);
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
            csBody.setAlignment(HorizontalAlignment.CENTER);

            //STYLE SON

            addExcelHeader(workbook,sheet);
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            int a = 6;
            int b = 0;
            for (StockDepotDto stok : dtosList) {
                a++;
                b++;
                XSSFRow row = sheet.createRow((short) a);
                row.createCell(0).setCellValue(b);
                row.getCell(0).setCellStyle(csBody);
                //İçerik Kısmı Bu Satırdan İtibaren Obje Bilgileri İle Doldurulur
                row.createCell(1).setCellValue(stok.getDrugCard().getDrugName());
                row.getCell(1).setCellStyle(csBody);
                row.createCell(2).setCellValue(stok.getDrugBarcode());
                row.getCell(2).setCellStyle(csBody);
                row.createCell(3).setCellValue(stok.getSerialNumber());
                row.getCell(3).setCellStyle(csBody);
                row.createCell(4).setCellValue(stok.getLotNo());
                row.getCell(4).setCellStyle(csBody);
                row.createCell(5).setCellValue(dateFormat.format(stok.getExpirationDate()));
                row.getCell(5).setCellStyle(csBody);
                row.createCell(6).setCellValue(stok.getPosition());
                row.getCell(6).setCellStyle(csBody);
                row.createCell(7).setCellValue(stok.getDepotStatus().getExplanation());
                row.getCell(7).setCellStyle(csBody);
                row.createCell(8).setCellValue(stok.getCustomerOrder().getCustomerOrderNo());
                row.getCell(8).setCellStyle(csBody);
                row.createCell(9).setCellValue(stok.getCustomerSupplyOrder().getSupplyOrderNo());
                row.getCell(9).setCellStyle(csBody);

            }

            FileOutputStream fileOut = new FileOutputStream("docs/stok_excel_"+user.getUsername()+".xlsx");
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();

            return 1;
        }catch (Exception e){
            throw new Exception("Stok Excel Oluşturma İşleminde Hata Oluştu.",e);
        }
    }

    private void addExcelHeader(XSSFWorkbook workbook,XSSFSheet sheet) throws IOException {
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


        //Başlık verilir
        XSSFRow rowHeader = sheet.createRow((short) 4);
        rowHeader.createCell(0).setCellValue("Stok İlaç Listesi");
        rowHeader.getCell(0).setCellStyle(csHeading);

        sheet.addMergedRegion(new CellRangeAddress(0, 2, 0, 1));
        //son parametre kolon sayısına eşit olmalı
        sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 9));


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
        rowhead.createCell(8).setCellValue("Yurtdışı Sİp No");
        rowhead.getCell(8).setCellStyle(csHeader);
        sheet.setColumnWidth(8, 5000);
        rowhead.createCell(9).setCellValue("Satın Alma Sip No");
        rowhead.getCell(9).setCellStyle(csHeader);
        sheet.setColumnWidth(9, 6000);

        //A4 Sayfaya Sığdırma
        sheet.setFitToPage(true);
        PrintSetup ps = sheet.getPrintSetup();
        ps.setFitWidth( (short) 1);
        ps.setFitHeight( (short) 0);
    }

    public Page<StockCountingExplanationDto> searchStockCountingExplanation(StockCountingExplanationSearchDto dto, Integer pageNo, Integer pageSize, String sortBy) {
        StringBuilder createSqlQuery = new StringBuilder("select * from depot d where d.depot_status_id = 10 ");


        if(dto.getStockCountingExplanation() != null) createSqlQuery.append("and d.stock_counting_explanation ILIKE  '%"+dto.getStockCountingExplanation()+"%' ");
        else createSqlQuery.append("and d.stock_counting_explanation is not null");

        if(pageNo==0){
            createSqlQuery.append(" order by d.depot_id limit "+pageSize+" offset "+pageNo);
        }else{
            createSqlQuery.append(" order by d.depot_id limit "+pageSize+" offset "+(pageSize*pageNo));
        }

        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Depot.class).getResultList();
        StockCountingExplanationDto[] dtos = mapper.map(list, StockCountingExplanationDto[].class);
        List<StockCountingExplanationDto> dtoList = Arrays.asList(dtos);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
        /*int start = Math.min((int)paging.getOffset(), dtoList.size());
        int end = Math.min((start + paging.getPageSize()), dtoList.size());*/

        int start=0;
        int end=dtoList.size();
        int totalCount=0;

        //Arama Kriterlerine Göre Toplam Kayıt Sayısı Başlangıç
        StringBuilder createSqlQueryCount = new StringBuilder("select count(*) from depot d where d.depot_status_id = 10 ");

        if(dto.getStockCountingExplanation() != null) createSqlQueryCount.append("and d.stock_counting_explanation ILIKE  '%"+dto.getStockCountingExplanation()+"%' ");
        else createSqlQuery.append("and d.stock_counting_explanation is not null");

        List<Object> countList = entityManager.createNativeQuery(createSqlQueryCount.toString()).getResultList();

        for(Object data :countList){
            totalCount= Integer.valueOf(String.valueOf((BigInteger) data));
        }
        //Arama Kriterlerine Göre Toplam Kayıt Sayısı Son

        Page<StockCountingExplanationDto> dtoPage = new PageImpl<>(dtoList.subList(start, end), paging, totalCount);

        return dtoPage;
    }

    public int createStockCountingExplanationExcel(String authHeader,StockCountingExplanationSearchDto dto) throws Exception {

        try {
            //ARAMA BAŞLANGIÇ
            StringBuilder createSqlQuery = new StringBuilder("select * from depot d where d.depot_status_id = 10 ");


            if(dto.getStockCountingExplanation() != null) createSqlQuery.append("and d.stock_counting_explanation ILIKE  '%"+dto.getStockCountingExplanation()+"%' ");
            else createSqlQuery.append("and d.stock_counting_explanation is not null");

            List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Depot.class).getResultList();
            StockCountingExplanationDto[] dtos = mapper.map(list, StockCountingExplanationDto[].class);
            List<StockCountingExplanationDto> dtosList = Arrays.asList(dtos);
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

            addExcelHeaderForStockCountingExplanation(workbook,sheet);
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            int a = 6;
            int b = 0;
            for (StockCountingExplanationDto stok : dtosList) {
                a++;
                b++;
                XSSFRow row = sheet.createRow((short) a);
                row.createCell(0).setCellValue(b);
                row.getCell(0).setCellStyle(csBody);
                //İçerik Kısmı Bu Satırdan İtibaren Obje Bilgileri İle Doldurulur
                row.createCell(1).setCellValue(stok.getDrugCard().getDrugName());
                row.getCell(1).setCellStyle(csBody);
                row.createCell(2).setCellValue("01"+stok.getItsNo()+"17"+sdf.format(stok.getExpirationDate())+"10"+stok.getLotNo());
                row.getCell(2).setCellStyle(csBody);
                row.createCell(3).setCellValue(stok.getItsNo());
                row.getCell(3).setCellStyle(csBody);
//                row.createCell(2).setCellValue(stok.getDrugBarcode());
//                row.getCell(2).setCellStyle(csBody);
//                row.createCell(3).setCellValue(stok.getSerialNumber());
//                row.getCell(3).setCellStyle(csBody);
//                row.createCell(4).setCellValue(stok.getLotNo());
//                row.getCell(4).setCellStyle(csBody);
//                row.createCell(5).setCellValue(dateFormat.format(stok.getExpirationDate()));
//                row.getCell(5).setCellStyle(csBody);

            }

            FileOutputStream fileOut = new FileOutputStream("docs/stock_explanation_excel_"+user.getUsername()+".xlsx");
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();

            return 1;
        }catch (Exception e){
            throw new Exception("Stok Açıklaması Excel Oluşturma İşleminde Hata Oluştu.",e);
        }
    }

    private void addExcelHeaderForStockCountingExplanation(XSSFWorkbook workbook,XSSFSheet sheet) throws IOException {
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
        rowDate.createCell(3).setCellValue(dateFormat.format(new Date()));
        rowDate.getCell(3).setCellStyle(csDate);


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
        rowHeader.createCell(0).setCellValue("İlaç Its Listesi");
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
        rowhead.createCell(2).setCellValue("Karekod");
        rowhead.getCell(2).setCellStyle(csHeader);
        sheet.setColumnWidth(2, 15000);
        rowhead.createCell(3).setCellValue("Its No");
        rowhead.getCell(3).setCellStyle(csHeader);
        sheet.setColumnWidth(3, 10000);
//        rowhead.createCell(2).setCellValue("Barkod");
//        rowhead.getCell(2).setCellStyle(csHeader);
//        sheet.setColumnWidth(2, 4500);
//        rowhead.createCell(3).setCellValue("Seri No");
//        rowhead.getCell(3).setCellStyle(csHeader);
//        sheet.setColumnWidth(3, 4500);
//        rowhead.createCell(4).setCellValue("Parti No");
//        rowhead.getCell(4).setCellStyle(csHeader);
//        sheet.setColumnWidth(4, 4000);
//        rowhead.createCell(5).setCellValue("SKT");
//        rowhead.getCell(5).setCellStyle(csHeader);
//        sheet.setColumnWidth(5, 3500);

        //A4 Sayfaya Sığdırma
        sheet.setFitToPage(true);
        PrintSetup ps = sheet.getPrintSetup();
        ps.setFitWidth( (short) 1);
        ps.setFitHeight( (short) 0);
    }
}