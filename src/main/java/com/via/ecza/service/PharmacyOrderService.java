package com.via.ecza.service;


import com.via.ecza.dto.*;
import com.via.ecza.entity.*;
import com.via.ecza.repo.*;
import javassist.NotFoundException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
public class PharmacyOrderService {

    @Autowired
    private ModelMapper mapper;
    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private CustomerSupplyOrderRepository customerSupplyOrderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ControlService controlService;
    @Autowired
    private SupplierOfferRepository supplierOfferRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private RefundRepository refundRepository;
    @Autowired
    private RefundOfferRepository refundOfferRepository;
    @Autowired
    private DepotRepository depotRepository;
    @Autowired
    private DepotStatusRepository depotStatusRepository;
    @Autowired
    private RefundOfferStatusRepository refundOfferStatusRepository;
    @Autowired
    private RefundStatusRepository refundStatusRepository;
    @Autowired
    private CustomerSupplyStatusRepository customerSupplyStatusRepository;
    @Autowired
    private OtherCompanyRepository otherCompanyRepository;
    @Autowired
    private PtsInformationRepository ptsInformationRepository;

    Date createdAt = new Date(System.currentTimeMillis());

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public List<PharmacyOrdesDto> getOrdersByPharmacy(String authHeader, SupplyCustomerOrderSearchDto dto) throws NotFoundException {
        User user = this.getUserFromToken(authHeader);
        Optional<Supplier> optionalSupplier = supplierRepository.getSupplierByUser(user.getUserId());
        if (!optionalSupplier.isPresent()) {
            throw new NotFoundException("Böyle bir eczane kullanıcı yok");
        }
        StringBuilder createSqlQuery = new StringBuilder("select * from customer_supply_order cso where cso.customer_supply_status_id between 9 and 51 ");


        if (dto.getCustomerOrderNo() != null) {
            createSqlQuery.append(" and supply_order_no  ILIKE '%" + dto.getCustomerOrderNo() + "%' ");
        }

        if (dto.getDrugCard() != null) {
            createSqlQuery.append("and drug_card_id=" + dto.getDrugCard() + " ");
        }

        if (dto.getOrderStatusId() != null) {
            createSqlQuery.append(" and customer_supply_status_id=" + dto.getOrderStatusId() + " ");
        }

        createSqlQuery.append(" and supplier_id=" + optionalSupplier.get().getSupplierId() + " ");

        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), CustomerSupplyOrder.class).getResultList();
        PharmacyOrdesDto[] dtos = mapper.map(list, PharmacyOrdesDto[].class);

        List<PharmacyOrdesDto> liste = Arrays.asList(dtos);


        return liste;
    }

//    //Eczane için siparişler listesi
//    public List<PharmacyOrdesDto> getOrdersByPharmacy(String authHeader) throws NotFoundException {
//        User user = this.getUserFromToken(authHeader);
//        Optional<Supplier> optionalSupplier = supplierRepository.getSupplierByUser(user.getUserId());
//        List<CustomerSupplyOrder> customerSupplyOrderList = customerSupplyOrderRepository.getOrdersBySupplier(optionalSupplier.get().getSupplierId());
//        PharmacyOrdesDto[] customerSuppliersDtos = mapper.map(customerSupplyOrderList, PharmacyOrdesDto[].class);
//
//        return Arrays.asList(customerSuppliersDtos);
//    }

    //Eczane için iadeler listesi
//    public List<PharmacyRefundDto> getRefundsBySupplier(String authHeader) throws NotFoundException {
//        User user = this.getUserFromToken(authHeader);
//        Optional<Supplier> optionalSupplier = supplierRepository.getSupplierByUser(user.getUserId());
//        List<Refund> refundList = refundRepository.getRefundsBySupplier(optionalSupplier.get().getSupplierId());
//        refundList.removeIf(c -> (c.getRefundStatus().getRefundStatusId() != 10));
//        PharmacyRefundDto[] pharmacyOrdesDtos = mapper.map(refundList, PharmacyRefundDto[].class);
//        return Arrays.asList(pharmacyOrdesDtos);
//    }

    public Boolean acceptRefundOfferByPurchase(Long refundOfferId) throws NotFoundException {
        Optional<RefundOffer> optionalRefundOffer = refundOfferRepository.findById(refundOfferId);
        if (!optionalRefundOffer.isPresent()) {
            throw new NotFoundException("Böyle bir sipariş yok");
        }
        if (optionalRefundOffer.get().getRefundOfferStatus().getRefundOfferStatusId() != 10L) {
            throw new NotFoundException("Bu sipariş daha önce değiştirilmiş");
        }
        List<Depot> depotList = depotRepository.acceptRefund(refundOfferId);
        if (depotList.size() < 1) {
            throw new NotFoundException("Bu ilaçlar hatalı olarak başka yerde kullanımda");
        }
        Refund refund = new Refund();
        // refund = refundRepository.save(refund);

        optionalRefundOffer.get().setRefundOfferStatus(refundOfferStatusRepository.findById(50L).get());
        refundOfferRepository.save(optionalRefundOffer.get());

        refund.setCreatedAt(createdAt);
        refund.setOtherCompanyId(optionalRefundOffer.get().getOtherCompanyId());
        refund.setExpirationDate(optionalRefundOffer.get().getExpirationDate());
        refund.setTotalPrice(optionalRefundOffer.get().getTotalPrice());
        refund.setUnitPrice(optionalRefundOffer.get().getUnitPrice());
        refund.setTotality(optionalRefundOffer.get().getTotality());
        refund.setDrugCard(optionalRefundOffer.get().getDrugCard());
        refund.setSupplier(optionalRefundOffer.get().getSupplier());
        refund.setRefundStatus(refundStatusRepository.findById(10L).get());
        refund = refundRepository.save(refund);
        refund.setRefundOrderNo(getCode(refund.getRefundId()));
        refund = refundRepository.save(refund);
        for (Depot drugs : depotList) {
            drugs.setRefundOffer(null);
            drugs.setDepotStatus(depotStatusRepository.findById(15L).get());
            drugs.setRefundOffer(null);
            drugs.setRefund(refund);
            depotRepository.save(drugs);
        }
        return true;

    }

    private String getCode(Long refundId) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String code = "IADE-" + year;
        int size = refundId.toString().length();
        for (int i = 0; i < 5 - size; i++)
            code += "0";
        code += refundId;
        return code;
    }


    public Page<PharmacyOrdesDto> getOrdersWithPage(String authHeader, PharmacyOrderSearchDto dto, Integer pageNo, Integer pageSize, String sortBy) throws Exception {

        //  Page<PharmacyOrdesDto> pageList = null;
        User user = this.getUserFromToken(authHeader);
        Optional<Supplier> optionalSupplier = supplierRepository.getSupplierByUser(user.getUserId());
        if(!optionalSupplier.isPresent()){throw new NotFoundException("Böyle bir eczane yok");}
        StringBuilder createSqlQuery = new StringBuilder("select * from customer_supply_order cso where supplier_id=" + optionalSupplier.get().getSupplierId() + " ");

        if (dto.getSupplyOrderNo() != null) {

            createSqlQuery.append("and cso.supply_order_no  ILIKE '%" + dto.getSupplyOrderNo() + "%' ");
        }

        if (dto.getDrugCard() != null) {

            createSqlQuery.append("and cso.drug_card_id=" + dto.getDrugCard() + " ");
        }

        if (dto.getCustomerSupplyStatus() != null)
            createSqlQuery.append("and cso.customer_supply_status_id=" + dto.getCustomerSupplyStatus() + " ");

        if (dto.getStartDate() != null)
            createSqlQuery.append(" and cso.created_at >= to_date('" + sdf.format(dto.getStartDate()) + "'," + "'dd.MM.yyyy') ");

        if (dto.getEndDate() != null)
            createSqlQuery.append(" and cso.created_at <= to_date('" + sdf.format(dto.getEndDate()) + "'," + "'dd.MM.yyyy') ");

        createSqlQuery.append(" order by cso.customer_supply_order_id DESC");


        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), CustomerSupplyOrder.class).getResultList();

        PharmacyOrdesDto[] dtos = mapper.map(list, PharmacyOrdesDto[].class);
        List<PharmacyOrdesDto> dtosList = Arrays.asList(dtos);

        for(PharmacyOrdesDto dtoo:dtosList){
            if(dtoo.getCustomerSupplyStatus().getCustomerSupplyStatusId()==10){
                dtoo.setStatusForPharmacy("İlaçları Göndermeniz Bekleniyor");
            }
            else if(dtoo.getCustomerSupplyStatus().getCustomerSupplyStatusId()==20){
                dtoo.setStatusForPharmacy("Siparişleri Eksik Gönderdiniz");
            }
            else if(dtoo.getCustomerSupplyStatus().getCustomerSupplyStatusId()==30){
                dtoo.setStatusForPharmacy("Sipariş Sizin Tarafınızdan İptal Edildi");
            }
            else if(dtoo.getCustomerSupplyStatus().getCustomerSupplyStatusId()==40){
                dtoo.setStatusForPharmacy("Ekip Pharma Tarafından İptal Edildi");
            }
            else if(dtoo.getCustomerSupplyStatus().getCustomerSupplyStatusId()==45){
                dtoo.setStatusForPharmacy("Teslim Ediliyor");
            }
            else if(dtoo.getCustomerSupplyStatus().getCustomerSupplyStatusId()==50){
                dtoo.setStatusForPharmacy("Teslim Edildi");
            }
            else {
                dtoo.setStatusForPharmacy("Yanlış Statü");
            }


if(dtoo.getOtherCompanyId()!=null){
            Optional<OtherCompany> optOtherCompany=otherCompanyRepository.findById(dtoo.getOtherCompanyId());
            if(optOtherCompany.isPresent()){
                dtoo.setOtherCompanyName(optOtherCompany.get().getOtherCompanyName());
            }
}


        }


        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
        int start = Math.min((int) paging.getOffset(), dtosList.size());
        int end = Math.min((start + paging.getPageSize()), dtosList.size());

        Page<PharmacyOrdesDto> pageList = new PageImpl<>(dtosList.subList(start, end), paging, dtosList.size());

        return pageList;


    }
    public Boolean getOrdersWithPDF(String authHeader, PharmacyOrderSearchDto dto) throws Exception {

        //  Page<PharmacyOrdesDto> pageList = null;
        User user = this.getUserFromToken(authHeader);
        Optional<Supplier> optionalSupplier = supplierRepository.getSupplierByUser(user.getUserId());
        if(!optionalSupplier.isPresent()){throw new NotFoundException("Böyle bir eczane yok");}
        StringBuilder createSqlQuery = new StringBuilder("select * from customer_supply_order cso where supplier_id=" + optionalSupplier.get().getSupplierId() + " ");

        if (dto.getSupplyOrderNo() != null) {

            createSqlQuery.append("and supply_order_no  ILIKE '%" + dto.getSupplyOrderNo() + "%' ");
        }

        if (dto.getDrugCard() != null) {

            createSqlQuery.append("and drug_card_id=" + dto.getDrugCard() + " ");
        }

        if (dto.getCustomerSupplyStatus() != null) {

            createSqlQuery.append("and customer_supply_status_id=" + dto.getCustomerSupplyStatus() + " ");
        }


        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), CustomerSupplyOrder.class).getResultList();

        PDFService pdfService=new PDFService();
        pdfService.createPDF(list,"Eczane Satış",user.getName(),10,new int[]{1,2,2,3,1,1,1,2,2,3},
                Arrays.asList("No","Kabul","SKT","İlaç Adı","Adet","MF Oranı","Toplam","Sipariş Numarası","Toplam Tutar","Durum"),
                "Eczane-Satın-Alma-Siparişleri");

        return true;


    }
    public Boolean getOrdersWithExcel(String authHeader, PharmacyOrderSearchDto dto) throws Exception {

        //  Page<PharmacyOrdesDto> pageList = null;
        User user = this.getUserFromToken(authHeader);
        Optional<Supplier> optionalSupplier = supplierRepository.getSupplierByUser(user.getUserId());
        if(!optionalSupplier.isPresent()){throw new NotFoundException("Böyle bir eczane yok");}
        StringBuilder createSqlQuery = new StringBuilder("select * from customer_supply_order cso where supplier_id=" + optionalSupplier.get().getSupplierId() + " ");

        if (dto.getSupplyOrderNo() != null) {

            createSqlQuery.append("and supply_order_no  ILIKE '%" + dto.getSupplyOrderNo() + "%' ");
        }

        if (dto.getDrugCard() != null) {

            createSqlQuery.append("and drug_card_id=" + dto.getDrugCard() + " ");
        }

        if (dto.getCustomerSupplyStatus() != null) {

            createSqlQuery.append("and customer_supply_status_id=" + dto.getCustomerSupplyStatus() + " ");
        }


        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), CustomerSupplyOrder.class).getResultList();

        ExcelService excelService=new ExcelService();
        excelService.createExcel(list,"Eczane Satış",user.getName(),10,
                new int[]{1500,3000,3000,8000,2200,2000,2200,4000,4000,4000},
                Arrays.asList("No","Kabul","SKT","İlaç Adı","Adet","MF Oranı","Toplam","Sipariş Numarası","Toplam Tutar","Durum"),
                "Eczane-Satın-Alma-Siparişleri-excel");


        return true;


    }

    public List<CustomerSupplyStatusForPharmacyDto> getAllOrderStatus() throws NotFoundException {
        CustomerSupplyStatusForPharmacyDto[] list =mapper.map( customerSupplyStatusRepository.getAllOrderStatus(),CustomerSupplyStatusForPharmacyDto[].class);

        for (CustomerSupplyStatusForPharmacyDto liste : list) {

            if (liste.getCustomerSupplyStatusId() == 10) {
                liste.setStatusName("İlaçları Göndermeniz Bekleniyor");
            }
            if (liste.getCustomerSupplyStatusId() == 20) {
                liste.setStatusName("Siparişleri Eksik Gönderdiniz");
            }
            if (liste.getCustomerSupplyStatusId() == 30) {
                liste.setStatusName("Sipariş Sizin Tarafınızdan İptal Edildi");
            }
            if (liste.getCustomerSupplyStatusId() == 40) {
                liste.setStatusName("Ekip Pharma Tarafından İptal Edildi");
            }
            if (liste.getCustomerSupplyStatusId() == 45) {
                liste.setStatusName("Teslim Ediliyor");
            }
            if (liste.getCustomerSupplyStatusId() == 50) {
                liste.setStatusName("Teslim Edildi");
            }

        }


        return Arrays.asList(list);
    }

    public List<RefundStatusForPharmacyDto> getAllRefundStatus() throws NotFoundException {
        RefundStatusForPharmacyDto[] list =mapper.map( refundStatusRepository.getAllRefundStatus(),RefundStatusForPharmacyDto[].class);

        for (RefundStatusForPharmacyDto liste : list) {

            if (liste.getRefundStatusId() == 10) {
                liste.setStatusName("İlaçların gelmesi bekleniyor");
            }
            if (liste.getRefundStatusId() == 20) {
                liste.setStatusName("İade sizin tarafınızdan reddedildi!");
            }
            if (liste.getRefundStatusId() == 30) {
                liste.setStatusName("İade işlemi Ekip Pharma tarafından iptal edildi");
            }
            if (liste.getRefundStatusId() == 40) {
                liste.setStatusName("Ekip Pharma tarafından iptal edildi");
            }
            if(liste.getRefundStatusId()==50){liste.setStatusName("İletildi");}

        }
        return Arrays.asList(list);
    }

    public List<PharmacyRefundDto> getRefundsBySupplier(String authHeader, RefundSearchDto dto) throws NotFoundException {
        User user = this.getUserFromToken(authHeader);
        Optional<Supplier> optionalSupplier = supplierRepository.getSupplierByUser(user.getUserId());
        if (!optionalSupplier.isPresent()) {
            throw new NotFoundException("Böyle bir eczane kullanıcı yok");
        }

        StringBuilder createSqlQuery = new StringBuilder("select * from refund where refund_status_id between 9 and 49 ");

        if (dto.getRefundOrderNo() != null) {
            createSqlQuery.append(" and refund_order_no  ILIKE '%" + dto.getRefundOrderNo() + "%' ");
        }

        if (dto.getDrugCard() != null) {
            createSqlQuery.append("and drug_card_id=" + dto.getDrugCard() + " ");
        }

        if (dto.getRefundStatus() != null) {
            createSqlQuery.append(" and refund_status_id=" + dto.getRefundStatus() + " ");
        }

        createSqlQuery.append(" and supplier_id=" + optionalSupplier.get().getSupplierId() + " ");

        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Refund.class).getResultList();
        PharmacyRefundDto[] dtos = mapper.map(list, PharmacyRefundDto[].class);

        List<PharmacyRefundDto> liste = Arrays.asList(dtos);


        return liste;
    }


    public Page<PharmacyRefundDto> getRefundsWithPage(String authHeader, RefundSearchDto dto, Integer pageNo, Integer pageSize, String sortBy) throws Exception {

        //  Page<PharmacyOrdesDto> pageList = null;
        User user = this.getUserFromToken(authHeader);
        Optional<Supplier> optionalSupplier = supplierRepository.getSupplierByUser(user.getUserId());
        StringBuilder createSqlQuery = new StringBuilder("select * from refund r where r.supplier_id=" + optionalSupplier.get().getSupplierId() + " ");

        if (dto.getRefundOrderNo() != null) {

            createSqlQuery.append("and r.refund_order_no ILIKE '%" + dto.getRefundOrderNo() + "%' ");
        }

        if (dto.getRefundStatus() != null) {

            createSqlQuery.append("and r.refund_status_id=" + dto.getRefundStatus() + " ");
        }

        if (dto.getDrugCard() != null) {

            createSqlQuery.append("and r.drug_card_id=" + dto.getDrugCard() + " ");
        }

        createSqlQuery.append("order by r.refund_id DESC");

        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Refund.class).getResultList();

        PharmacyRefundDto[] dtos = mapper.map(list, PharmacyRefundDto[].class);

        for (PharmacyRefundDto item :dtos) {
            if(item.getOtherCompanyId()!=null) {
                Optional<OtherCompany> optOtherCompany = otherCompanyRepository.findById(item.getOtherCompanyId());
                if (optOtherCompany.isPresent()) {
                    item.setOtherCompanyName(optOtherCompany.get().getOtherCompanyName());
                }
            }
        }


        List<PharmacyRefundDto> dtosList = Arrays.asList(dtos);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
        int start = Math.min((int) paging.getOffset(), dtosList.size());
        int end = Math.min((start + paging.getPageSize()), dtosList.size());

        Page<PharmacyRefundDto> pageList = new PageImpl<>(dtosList.subList(start, end), paging, dtosList.size());

        return pageList;


    }
    public Boolean getRefundsWithPDF(String authHeader, RefundSearchDto dto) throws Exception {

        //  Page<PharmacyOrdesDto> pageList = null;
        User user = this.getUserFromToken(authHeader);
        Optional<Supplier> optionalSupplier = supplierRepository.getSupplierByUser(user.getUserId());
        StringBuilder createSqlQuery = new StringBuilder("select * from refund r where supplier_id=" + optionalSupplier.get().getSupplierId() + " ");

        if (dto.getRefundOrderNo() != null) {

            createSqlQuery.append("and refund_order_no ILIKE '%" + dto.getRefundOrderNo() + "%' ");
        }

        if (dto.getRefundStatus() != null) {

            createSqlQuery.append("and refund_status_id=" + dto.getRefundStatus() + " ");
        }

        if (dto.getDrugCard() != null) {

            createSqlQuery.append("and drug_card_id=" + dto.getDrugCard() + " ");
        }

        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Refund.class).getResultList();


        PDFService pdfService=new PDFService();
        pdfService.createPDF(list,"Eczane İade",user.getName(),9,new int[]{1,2,2,4,2,2,2,2,2},
                Arrays.asList("No ","İade No","Kabul","İlaç Adı","SKT","Adet","Adet Fiyatı","Toplam Tutar","Durum"),
                "Eczane-İade-Siparişleri");



        return true;


    }
    public Boolean getRefundsWithExcel(String authHeader, RefundSearchDto dto) throws Exception {

        //  Page<PharmacyOrdesDto> pageList = null;
        User user = this.getUserFromToken(authHeader);
        Optional<Supplier> optionalSupplier = supplierRepository.getSupplierByUser(user.getUserId());
        StringBuilder createSqlQuery = new StringBuilder("select * from refund r where supplier_id=" + optionalSupplier.get().getSupplierId() + " ");

        if (dto.getRefundOrderNo() != null) {

            createSqlQuery.append("and refund_order_no ILIKE '%" + dto.getRefundOrderNo() + "%' ");
        }

        if (dto.getRefundStatus() != null) {

            createSqlQuery.append("and refund_status_id=" + dto.getRefundStatus() + " ");
        }

        if (dto.getDrugCard() != null) {

            createSqlQuery.append("and drug_card_id=" + dto.getDrugCard() + " ");
        }

        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Refund.class).getResultList();


        ExcelService excelService=new ExcelService();
        excelService.createExcel(list,"Eczane İade",user.getName(),
                9,
                new int[]{1500,5000,4000,10000,4000,5000,6000,6000,8000},
                Arrays.asList("No ","İade No","Kabul","İlaç Adı","SKT","Adet","Adet Fiyatı","Toplam Tutar","Durum"),
                "Eczane-İade-Siparişleri-Excel");




        return true;


    }

    private User getUserFromToken(String authHeader) throws NotFoundException {

        String username = controlService.getUsernameFromToken(authHeader);
        Optional<User> optUser = userRepository.findByUsername(username);
        if (!optUser.isPresent()) {
            throw new NotFoundException("Not found User");
        }
        return optUser.get();
    }

    // Eczane Modülü Ana Sayfa Pie Cart Yıllık Satışı Yapılan İlaç Miktarı
    public int getPharmacyTotalAmountOfDrugs (String authHeader) throws NotFoundException {
        Date date = new Date();
        int year = date.getYear() + 1900;
        User user = controlService.getUserFromToken(authHeader);
        Optional<Supplier> optionalSupplier = supplierRepository.getSupplierByUser(user.getUserId());
        StringBuilder createSqlQueryForDepot = new StringBuilder("select sum(cso.totality) from customer_supply_order cso where extract(year from cso.created_at)="+year+"  and cso.supplier_id =" +
                optionalSupplier.get().getSupplierId());
        List<Object> supplyOrder = entityManager.createNativeQuery(createSqlQueryForDepot.toString()).getResultList();
        int orderCount = Integer.valueOf(JSONObject.valueToString(supplyOrder.get(0)));
        return orderCount;

    }

    // Eczane Modülü Ana Sayfa Pie Cart Yıllık Satışı Yapılan İlaç Miktarı
    public int getPharmacyTotalReturnDrugs (String authHeader) throws NotFoundException {
        Date date = new Date();
        int year = date.getYear() + 1900;
        User user = controlService.getUserFromToken(authHeader);
        Optional<Supplier> optionalSupplier = supplierRepository.getSupplierByUser(user.getUserId());
        StringBuilder createSqlQueryForDepot = new StringBuilder("select sum(r.totality) from refund r where r.refund_status_id = 50 and extract(year from r.created_at)="+year+" and r.supplier_id =" +
                optionalSupplier.get().getSupplierId());
        List<Object> supplyOrder = entityManager.createNativeQuery(createSqlQueryForDepot.toString()).getResultList();
        int orderCount = Integer.valueOf(JSONObject.valueToString(supplyOrder.get(0)));
        return orderCount;

    }

    public Boolean UpdateFromExcel(String authHeader,String fileName) throws Exception {
        try {
            User user = controlService.getUserFromToken(authHeader);


            HashMap<Integer, PtsInformation> newMap = readPtsExcel(fileName);

            if (newMap.size() <= 0) {
                throw new Exception("Pts Excel Okunamadı !");
            }

            newMap.keySet().stream().forEach(x -> {
                try {
                    savePtsInfo(newMap.get(x));
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

    public HashMap<Integer, PtsInformation> readPtsExcel(String fileName) throws Exception {
        int cellNumber = 0;

        try {


            InputStream file = new FileInputStream(new File("docs/" + fileName + ".xlsx"));
            XSSFWorkbook workbook = new XSSFWorkbook(file);


            // Excel Dosyasının Hangi Sayfası İle Çalışacağımızı Seçelim.
            XSSFSheet sheet = workbook.getSheetAt(0);
            // Belirledigimiz sayfa icerisinde tum satirlari tek tek dolasacak iterator nesnesi
            Iterator rowIterator = sheet.iterator();
            Row row = (Row) rowIterator.next();       //ilk satır başlık olduğu için geçildi



            int counter = 0;

            String boxBarcodeFromExcel=null;


            // Okunacak Satir Oldugu Surece
            HashMap<Integer, PtsInformation> yeniMap = new HashMap<>();

            while (rowIterator.hasNext()) {
                cellNumber = 0;//yeni satıra geçince hücre sayısı baştan başlıyor
                // Excel içerisindeki satiri temsil eden nesne
                row = (Row) rowIterator.next();
                // Her bir satir icin tum hucreleri dolasacak iterator nesnesi
                Iterator cellIterator = row.cellIterator();

                PtsInformationDto ptsInfo = new PtsInformationDto(null,null,null,0,new Date());
                counter++;


                while (cellIterator.hasNext()) {
                    cellNumber++;
                    // Excel icerisindeki hucreyi temsil eden nesne
                    Cell cell = (Cell) cellIterator.next();


                    switch (cellNumber) {
                        case 1:
                            switch (cell.getCellType()) {
                                case STRING:
                                    ptsInfo.setBoxBarcode(cell.getStringCellValue().trim());
                                    break;

                                case BLANK:
                                    //System.out.println("Alan Boş");
                                    break;

                                case NUMERIC:
                                    ptsInfo.setBoxBarcode(String.valueOf((long) cell.getNumericCellValue()).trim());
                                    break;
                            }

                            break;
                        case 2:
                            switch (cell.getCellType()) {
                                case STRING:
                                    ptsInfo.setDrugQrCode(cell.getStringCellValue().trim());
                                    break;

                                case BLANK:
                                    // System.out.println("Alan Boş");
                                    break;

                                case NUMERIC:
                                    ptsInfo.setDrugQrCode(String.valueOf(cell.getNumericCellValue()).trim());
                                    break;
                            }

                            break;
                    }

                }


                if(ptsInfo.getBoxBarcode()!=null && ptsInfo.getDrugQrCode()!=null && ptsInfo.getBoxBarcode().length()>0 && ptsInfo.getDrugQrCode().length()>0 && ptsInfo.getBoxBarcode().equals(ptsInfo.getDrugQrCode())){
                    boxBarcodeFromExcel=ptsInfo.getBoxBarcode();
                    if(ptsInformationRepository.qrCodeListFromBarcode(boxBarcodeFromExcel)>0){
                        //okunan barkoda ait önceden kayıt varsa statusu 1 yapılır
                        ptsInformationRepository.updatePreviousBoxBarcodeStatusTo1(boxBarcodeFromExcel.trim());
                    }
                }else if(boxBarcodeFromExcel!=null && ptsInfo.getBoxBarcode()!=null && ptsInfo.getDrugQrCode()!=null && ptsInfo.getBoxBarcode().length()>0 && ptsInfo.getDrugQrCode().length()>0 && !ptsInfo.getBoxBarcode().equals(ptsInfo.getDrugQrCode())){
                    ptsInfo.setDrugQrCode(editQrCode(ptsInfo.getDrugQrCode()));
                    ptsInfo.setBoxBarcode(boxBarcodeFromExcel);
                    yeniMap.put(counter,(mapper.map(ptsInfo, PtsInformation.class)));
                }
            }


            // yeniMap.keySet().stream().forEach(x -> System.out.println(x+"--"+yeniMap.get(x).getDrugQrCode()+"--"+yeniMap.get(x).getBoxBarcode()));
            file.close();
            workbook.close();

            return yeniMap;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        return null;


    }

    public static String editQrCode(String qrCode) throws Exception {
        //karekoda & ayracı ekleme
        String finalQrcode = "";
        StringBuilder barcode = new StringBuilder();
        StringBuilder serialNo = new StringBuilder();
        StringBuilder restOfQrCode = new StringBuilder();
        int expDateStartIndex = 0;
        qrCode = qrCode.trim();
        //İlaç Barkod Kontrolü
        if (qrCode.length() > 29) {
            if (qrCode.charAt(0) == '0' && qrCode.charAt(1) == '1') {
                barcode.append(qrCode.substring(0, 16)); // 01 dahil edildi.
            }

            //İlaç Seri Numarası Kontrolü
            if (qrCode.charAt(16) == '2' && qrCode.charAt(17) == '1') {
                String expDate = "";

                // 19   20   21 22 23 24 25 26
                expDateStartIndex = qrCode.indexOf("17", 18);

                if (expDateStartIndex > 0  /* && partiNoStartIndex>0*/) {

                    // 17AABBCC10 ile oluşan bölumdekı 17 karakterındekı 1 in index numarasını arıyor.
                    expDateStartIndex = getIndexFromQrcodeFor17(expDateStartIndex, qrCode);
                    // 21 dahil edildi.
                    serialNo.append(qrCode.substring(16, expDateStartIndex));
                    serialNo.append("&");
                    // 17 dahil edildi.
                    restOfQrCode.append(qrCode.substring(expDateStartIndex));
                    finalQrcode = barcode.toString() + serialNo.toString() + restOfQrCode.toString();



                } else {
                    throw new Exception("karekod okumada hata oluştu !");
                }
            }
        }

        return finalQrcode;
    }

    private static int getIndexFromQrcodeFor17(int expDateStartIndex, String qrCode) {
        //tarih alanı komple sayısal ise ayraçta dahil -->17201130
        if (qrCode.substring(expDateStartIndex, expDateStartIndex + 8).matches("[0-9]+")) {
            int preDateCharachter = Integer.valueOf(qrCode.substring(expDateStartIndex, expDateStartIndex + 2));
            int partiNo = Integer.valueOf(qrCode.substring(expDateStartIndex + 8, expDateStartIndex + 10));
            int month =Integer.valueOf(qrCode.substring(expDateStartIndex + 4, expDateStartIndex + 6));
            int day =Integer.valueOf(qrCode.substring(expDateStartIndex + 6, expDateStartIndex + 8));

            if (preDateCharachter == 17 && partiNo == 10 && month>=1 && month<=12 && day>=1 && day<=31) {
                return expDateStartIndex;
            } else {
                return getIndexFromQrcodeFor17(++expDateStartIndex, qrCode);
            }
        } else {
            return getIndexFromQrcodeFor17(++expDateStartIndex, qrCode);
        }
    }

    public PtsInformation savePtsInfo(PtsInformation dto) throws Exception {

        PtsInformation pts=null;
        try {
            pts= ptsInformationRepository.save(dto);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pts;
    }


}
