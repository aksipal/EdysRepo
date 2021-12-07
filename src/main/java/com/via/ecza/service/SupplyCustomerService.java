package com.via.ecza.service;

import com.itextpdf.text.DocumentException;
import com.via.ecza.dto.*;
import com.via.ecza.entity.*;

import com.via.ecza.repo.*;
import javassist.NotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;


import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
public class SupplyCustomerService {


    //@PersistenceContext // or even @Autowired
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private CustomerOrderDrugsRepository customerOrderDrugsRepository;
    @Autowired
    private SupplyCustomerOrderRepository supplyCustomerOrderRepository;
    @Autowired
    private CustomerSupplyOrderRepository customerSupplyOrderRepository;
    @Autowired
    private DrugCardRepository drugCardRepository;
    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private ControlService controlService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DepotRepository depotRepository;
    @Autowired
    private CustomerOrderRepository customerOrderRepository;
    @Autowired
    private DepotStatusRepository depotStatusRepository;
    @Autowired
    private PurchaseOrderDrugsRepository purchaseOrderDrugsRepository;
    @Autowired
    private CustomerSupplyStatusRepository customerSupplyStatusRepository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    PurchaseStatusRepository purchaseStatusRepository;
    @Autowired
    CustomerOrderStatusRepository customerOrderStatusRepository;
    @Autowired
    private PDFService pdfService;
    @Autowired
    ExcelService excelService;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private  SupplierOfferRepository supplierOfferRepository;
    @Autowired
    private  CustomerOrderStatusHistoryService customerOrderStatusHistoryService;
    @Autowired
    private CustomerOrderStatusRepository orderStatusRepository;

    Date createdAt = new Date(System.currentTimeMillis());

    public Page<SupplyCustomerListDto> search(Pageable page, SupplyCustomerOrderSearchDto dto) throws Exception {
        StringBuilder createSqlQuery = new StringBuilder("select co.* from customer_order co  join customer_order_drugs cod on co.customer_order_id=cod.customer_order_id where  co.status = 1 and co.order_status_id<50 and cod.purchase_order_status=1 ");

        if (dto.getCustomerOrderNo() != null) {
            createSqlQuery.append("and  co.customer_order_no ILIKE '%" + dto.getCustomerOrderNo() + "%' ");
        }
        if (dto.getUserId() != null) {
            createSqlQuery.append("and co.user_id= " + dto.getUserId() + " ");
        }

        createSqlQuery.append(" group by co.customer_order_id  order by order_date");

        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), CustomerOrder.class).getResultList();
        SupplyCustomerListDto[] dtos = mapper.map(list, SupplyCustomerListDto[].class);
        List<SupplyCustomerListDto> dtosList = Arrays.asList(dtos);

        int start = Math.min((int) page.getOffset(), dtosList.size());
        int end = Math.min((start + page.getPageSize()), dtosList.size());

        Page<SupplyCustomerListDto> pageList = new PageImpl<>(dtosList.subList(start, end), page, dtosList.size());

        for (SupplyCustomerListDto scl : pageList) {
            scl.setPurchaseStatus(getAllStatus(scl.getCustomerOrderId()));
        }

        return pageList;
    }

    public List<SupplyCustomerListDto> search(SupplyCustomerOrderSearchDto dto) throws NotFoundException {

        StringBuilder createSqlQuery = new StringBuilder("select co.* from customer_order co  join customer_order_drugs cod on co.customer_order_id=cod.customer_order_id where  co.status = 1 and co.order_status_id<50 and cod.purchase_order_status=1 ");

        if (dto.getCustomerOrderNo() != null) {
            createSqlQuery.append("and  co.customer_order_no ILIKE '%" + dto.getCustomerOrderNo() + "%' ");
        }
        if (dto.getUserId() != null) {
            createSqlQuery.append("and co.user_id= " + dto.getUserId() + " ");
        }

        createSqlQuery.append(" group by co.customer_order_id  order by order_date");
        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), CustomerOrder.class).getResultList();
        SupplyCustomerListDto[] dtos = mapper.map(list, SupplyCustomerListDto[].class);

        List<SupplyCustomerListDto> liste = Arrays.asList(dtos);

        for (SupplyCustomerListDto scl : liste) {
            scl.setPurchaseStatus(getAllStatus(scl.getCustomerOrderId()));
        }
        return liste;
    }

    public PurchaseStatus getAllStatus(Long customerOrderId) throws NotFoundException {
        Optional<CustomerOrder> optionalCustomerOrder = customerOrderRepository.findById(customerOrderId);
        if (optionalCustomerOrder.get().getPurchaseOrderDrugs().size() < 1) {
            throw new NotFoundException("Yanlış Id Gönderdiniz! " + optionalCustomerOrder.get().getCustomerOrderId() + " No lu İhracat Idsi Altında Herhangi Bir İlaç Siparişi Yok. Lütfen Admine Başvurunuz!");
        }
        PurchaseStatus ps = new PurchaseStatus();
        ps.setPurchaseStatusId(100L);
        for (PurchaseOrderDrugs pod : optionalCustomerOrder.get().getPurchaseOrderDrugs()) {
            if (pod.getPurchaseStatus().getPurchaseStatusId() < ps.getPurchaseStatusId()) {
                ps = pod.getPurchaseStatus();
            }
        }
        if (ps.getPurchaseStatusId() == 100L) {
            ps.setPurchaseStatusId(10L);
        }//önlem amaçlı
        return ps;
    }

    //Siparişten istenilen ilaçları çekme
    public Page<SupplyCustomerDrugsDto> findByCustomerId(SupplySearchDrugsDto dto, Integer pageNo, Integer pageSize, String sortBy) throws NotFoundException {
        Optional<CustomerOrder> optCustomerOrder = supplyCustomerOrderRepository.findByCustomerId(dto.getCustomerOrderId());
        if (!optCustomerOrder.isPresent()) {
            throw new NotFoundException("Böyle Bir Yurt Dışı Siparişi Yok");
        }
        StringBuilder createSqlQuery = new StringBuilder("select * from purchase_order_drugs pod where customer_order_id=" + dto.getCustomerOrderId() + " ");


        if (dto.getDrugCard() != null) {

            createSqlQuery.append("and drug_card_id=" + dto.getDrugCard() + " ");
        }

        if (dto.getPurchaseStatusId() != null) {

            createSqlQuery.append("and purchase_status_id=" + dto.getPurchaseStatusId() + " ");
        }

        createSqlQuery.append("order by purchase_order_drugs_id ");


        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), PurchaseOrderDrugs.class).getResultList();

        SupplyCustomerDrugsDto[] dtos = mapper.map(list, SupplyCustomerDrugsDto[].class);
        List<SupplyCustomerDrugsDto> dtosList = Arrays.asList(dtos);
        int sum=0;
        for(SupplyCustomerDrugsDto liste:dtosList ){
           if(supplierOfferRepository.getSumOfOffers(liste.getPurchaseOrderDrugsId(), liste.getDrugCard().getDrugCardId())!=null){
               liste.setSumOfOffers(supplierOfferRepository.getSumOfOffers(liste.getPurchaseOrderDrugsId(), liste.getDrugCard().getDrugCardId()));
           }
           else{
               liste.setSumOfOffers(0);
           }

        }

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
        int start = Math.min((int) paging.getOffset(), dtosList.size());
        int end = Math.min((start + paging.getPageSize()), dtosList.size());

        Page<SupplyCustomerDrugsDto> pageList = new PageImpl<>(dtosList.subList(start, end), paging, dtosList.size());

        return pageList;

    }

    public List<PurchaseStatus> getAllPurchaseStatus() {
        return purchaseStatusRepository.getAllPurchaseStatus();
    }

    private String getCode(Long customerSupplyOrderId) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String code = "SAT-" + year;
        int size = customerSupplyOrderId.toString().length();
        for (int i = 0; i < 5 - size; i++)
            code += "0";
        code += customerSupplyOrderId;
        return code;
    }

    public Boolean save(@Valid CustomerSupplyOrderDto dto) throws Exception {

        Optional<PurchaseOrderDrugs> optionalPurchaseOrderDrugs = purchaseOrderDrugsRepository.findById(dto.getPurchaseOrderDrugs());
        if (!optionalPurchaseOrderDrugs.isPresent()) {
            throw new NotFoundException("Böyle Bir Sipariş Yok");
        }
        Optional<DrugCard> optionalDrugCard = drugCardRepository.findById(dto.getDrugCardId());
        if (!optionalDrugCard.isPresent()) {
            throw new NotFoundException("Boyle Bir İlaç Yok");
        }
        Optional<Supplier> optionalSupplier = supplierRepository.findById(dto.getSupplierId());
        if (!optionalSupplier.isPresent()) {
            throw new NotFoundException("Boyle Bir Tedarikçi Yok");
        }

        CustomerSupplyOrder customerSupplyOrder = mapper.map(dto, CustomerSupplyOrder.class);
        customerSupplyOrder.setCustomerSupplyStatus(customerSupplyStatusRepository.findById(10L).get());//Sipariş verildi bekleniyor gelmesi

        customerSupplyOrder.setPurchaseOrderDrugs(optionalPurchaseOrderDrugs.get());
        customerSupplyOrder.setDrugCard(optionalDrugCard.get());
        customerSupplyOrder.setSupplier(optionalSupplier.get());
        customerSupplyOrder.setStatus(1);
        customerSupplyOrder.setOtherCompanyId(dto.getOtherCompanyId());

        //stokta boşta duran ilaç hala duruyor mu kontrolü
        if (dto.getSupplierId() == 1) {
            DepotDto depotDto = new DepotDto();
            depotDto.setExpirationDate(dto.getExpirationDate());

            if (dto.getQuantity() > optionalPurchaseOrderDrugs.get().getIncompleteQuantity()) {
                throw new NotFoundException(" İhtiyaçtan Fazla Sipariş Stoktan Temin Edilemez");
            }

            //Depo kısmı(Volkan) için böyle yaptık
            customerSupplyOrder.setTotalQuantity(dto.getTotality());
            customerSupplyOrder.setCreatedAt(createdAt);
            if (this.getStockByOrder(dto.getDrugCardId(), depotDto) < dto.getQuantity()) {
                return false;
            }
        }

        //İhracat kısmı için sipariş statusunu depo teslime çekme
        Optional<CustomerOrder> co = customerOrderRepository.findById(optionalPurchaseOrderDrugs.get().getCustomerOrder().getCustomerOrderId());
        if (co.get().getOrderStatus().getOrderStatusId() < 32L) {
            co.get().setOrderStatus(customerOrderStatusRepository.findById(30L).get());
            if(!customerOrderStatusHistoryService.save(co.get(),co.get().getOrderStatus()))
                throw new Exception("Sipariş Oluşturulamadı");
        }

        optionalPurchaseOrderDrugs.get().setChargedQuantity(optionalPurchaseOrderDrugs.get().getChargedQuantity() + (customerSupplyOrder.getTotality() - customerSupplyOrder.getStocks()));
        optionalPurchaseOrderDrugs.get().setIncompleteQuantity(optionalPurchaseOrderDrugs.get().getTotalQuantity() - optionalPurchaseOrderDrugs.get().getChargedQuantity());
        setPurchases(optionalPurchaseOrderDrugs.get());

        customerSupplyOrderRepository.save(customerSupplyOrder);
        customerSupplyOrder.setSupplyOrderNo(getCode(customerSupplyOrder.getCustomerSupplyOrderId()));//eklemeyi unutma
        customerSupplyOrder = customerSupplyOrderRepository.save(customerSupplyOrder);
        if (dto.getSupplierId() == 1) {
            SupplyDepotOfferDto sdo = new SupplyDepotOfferDto();
            sdo.setCustomerSupplyOrder(customerSupplyOrder.getCustomerSupplyOrderId());
            sdo.setDrugCard(dto.getDrugCardId());
            sdo.setLimitation(dto.getQuantity());
            sdo.setCustomerOrder(customerSupplyOrder.getPurchaseOrderDrugs().getCustomerOrder().getCustomerOrderId());
            sdo.setExpirationDate(dto.getExpirationDate());

            if (setStockByOrder(sdo)) {

                return true;
            } else {
                return false;
            }
        }

        return true;

    }


    //İlaca bağlı siparişler listesi
    public List<CustomerSuppliersDto> findByPurchaseOrderDrugs(Long pharmacyOrderDrugs) throws NotFoundException {

        List<CustomerSupplyOrder> list = customerSupplyOrderRepository.findByPurchaseOrderDrugsId(pharmacyOrderDrugs);
        CustomerSuppliersDto[] array = mapper.map(list, CustomerSuppliersDto[].class);
        List<CustomerSuppliersDto> dtoList = Arrays.asList(array);


        return dtoList;
    }

    public Boolean pdfCreateForOrders(Long pharmacyOrderDrugs) throws DocumentException, NotFoundException, IOException {
        List<CustomerSupplyOrder> list = customerSupplyOrderRepository.findByPurchaseOrderDrugsId(pharmacyOrderDrugs);
        // CustomerSuppliersDto[] array = mapper.map(list, CustomerSuppliersDto[].class);
        //List<CustomerSuppliersDto> dtoList = Arrays.asList(array);

        Object[] liste = mapper.map(list, Object[].class);
        List<Object> liste2 = Arrays.asList(liste);

        StringBuilder sb = new StringBuilder(list.get(0).getPurchaseOrderDrugs().getCustomerOrder().getCustomerOrderNo());

        pdfService.createPDF(liste2, "Satın Alma", sb + "  İçin Sipariş Listesi ", 8, new int[]{1, 3, 2, 1, 1, 2, 2, 3}, Arrays.asList("No", " İlaç Adı ", "Sipariş Kodu", "Sipariş", "Stok", "Eczane Adı", "Toplam Fiyat", "Durum"), "Satın-Alma-Siparişleri");

        return true;
    }

    public Boolean excelCreateForOrders(Long pharmacyOrderDrugs) throws DocumentException, NotFoundException, IOException {
        List<CustomerSupplyOrder> list = customerSupplyOrderRepository.findByPurchaseOrderDrugsId(pharmacyOrderDrugs);
        // CustomerSuppliersDto[] array = mapper.map(list, CustomerSuppliersDto[].class);
        //List<CustomerSuppliersDto> dtoList = Arrays.asList(array);

        Object[] liste = mapper.map(list, Object[].class);
        List<Object> liste2 = Arrays.asList(liste);

        StringBuilder sb = new StringBuilder(list.get(0).getPurchaseOrderDrugs().getCustomerOrder().getCustomerOrderNo());

        excelService.createExcel(liste2, "Satın Alma", sb.toString(), 8, new int[]{1500, 10000, 4500, 4500, 4000, 3500, 4000, 10000}, Arrays.asList("No", "İlaç Adı", "İndirimli Birim Fiyat", "Adet", "Mal Fazlası Oranı", "Toplam Adet", "Toplam Fiyat", "Durum"), "satin_alma_excel");

        return true;
    }


    //CustomerSupplyOrder ı iptal işlemi
    public Boolean cancelCustomerSupplyOrder(String authHeader, Long customerSupplyOrderId) throws NotFoundException {

        User user = this.getUserFromToken(authHeader);
        Optional<CustomerSupplyOrder> optionalCustomerSupplyOrder = customerSupplyOrderRepository.findById(customerSupplyOrderId);
        if (!optionalCustomerSupplyOrder.isPresent()) {
            throw new NotFoundException("Böyle Bir Sipariş Yok");
        }
        if (optionalCustomerSupplyOrder.get().getCustomerSupplyStatus().getCustomerSupplyStatusId() != 10L) {
            throw new NotFoundException("Bu sipariş statusu daha önce değiştirilmiş iptal edemezsiniz !!!");
        }
        CustomerSupplyOrder customerSupplyOrder = optionalCustomerSupplyOrder.get();
        Optional<PurchaseOrderDrugs> optionalPurchaseOrderDrugs = purchaseOrderDrugsRepository.findById(customerSupplyOrder.getPurchaseOrderDrugs().getPurchaseOrderDrugsId());
        if (!optionalPurchaseOrderDrugs.isPresent()) {
            throw new NotFoundException("Böyle Bir Müşteri Siparişi Yok (CustomerOrder)");
        }

        if (optionalCustomerSupplyOrder.get().getSupplier().getSupplierId() == 1L) {

            if (reverseOrderByStock(customerSupplyOrderId)) {
                optionalPurchaseOrderDrugs.get().setChargedQuantity(optionalPurchaseOrderDrugs.get().getChargedQuantity() - (customerSupplyOrder.getTotality() - customerSupplyOrder.getStocks()));
                optionalPurchaseOrderDrugs.get().setIncompleteQuantity(optionalPurchaseOrderDrugs.get().getTotalQuantity() - optionalPurchaseOrderDrugs.get().getChargedQuantity());
                setPurchases(optionalPurchaseOrderDrugs.get());
                customerSupplyOrder.setLog_cso(customerSupplyOrder.getLog_cso() + "\n" + user.getUsername() + " Tarafından İptal Edidi");
                customerSupplyOrder.setCustomerSupplyStatus(customerSupplyStatusRepository.findById(40L).get());//eski -2 satın almacı diptal etme durumu
                customerSupplyOrder = customerSupplyOrderRepository.save(customerSupplyOrder);
                //Verilen sipariş satın almacı tarafından iptal edilme durumu
                return true;
            } else {
                return false;
            }
        } else {

            optionalPurchaseOrderDrugs.get().setChargedQuantity(optionalPurchaseOrderDrugs.get().getChargedQuantity() - (customerSupplyOrder.getTotality() - customerSupplyOrder.getStocks()));
            optionalPurchaseOrderDrugs.get().setIncompleteQuantity(optionalPurchaseOrderDrugs.get().getTotalQuantity() - optionalPurchaseOrderDrugs.get().getChargedQuantity());
            setPurchases(optionalPurchaseOrderDrugs.get());
            customerSupplyOrder.setLog_cso(customerSupplyOrder.getLog_cso() + "\n" + user.getUsername() + " Tarafından İptal Edidi");
            customerSupplyOrder.setCustomerSupplyStatus(customerSupplyStatusRepository.findById(40L).get());//eski -2 satın almacı diptal etme durumu
            customerSupplyOrder = customerSupplyOrderRepository.save(customerSupplyOrder);
            //Verilen sipariş satın almacı tarafından iptal edilme durumu
            return true;
        }

    }

    //Satın alma için eczane siparişler listesi
    public Page<PharmacyOrdesDto> getOrdersBySupplier(PharmacyOrderSearchDto dto, Integer pageNo, Integer pageSize, String sortBy) throws NotFoundException {
        Optional<Supplier> optionalSupplier = supplierRepository.findById(dto.getSupplierId());
        if (!optionalSupplier.isPresent()) {
            throw new NotFoundException("Sistemde Kayıtlı Böyle Bir Eczane Yok");
        }
        StringBuilder createSqlQuery = new StringBuilder("select * from customer_supply_order cso where supplier_id=" + optionalSupplier.get().getSupplierId() + " ");

        if (dto.getSupplyOrderNo() != null) {

            createSqlQuery.append("and supply_order_no  ILIKE '%" + dto.getSupplyOrderNo().trim() + "%' ");
        }

        if (dto.getDrugCard() != null) {

            createSqlQuery.append("and drug_card_id=" + dto.getDrugCard() + " ");
        }

        if (dto.getCustomerSupplyStatus() != null) {

            createSqlQuery.append("and customer_supply_status_id=" + dto.getCustomerSupplyStatus() + " ");
        }


        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), CustomerSupplyOrder.class).getResultList();

        PharmacyOrdesDto[] dtos = mapper.map(list, PharmacyOrdesDto[].class);
        List<PharmacyOrdesDto> dtosList = Arrays.asList(dtos);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
        int start = Math.min((int) paging.getOffset(), dtosList.size());
        int end = Math.min((start + paging.getPageSize()), dtosList.size());

        Page<PharmacyOrdesDto> pageList = new PageImpl<>(dtosList.subList(start, end), paging, dtosList.size());

        return pageList;


    }


    // Sipariş için verilen skt  ya göre stoktan ilaç ayırma
    public Boolean setStockByOrder(SupplyDepotOfferDto dto) throws NotFoundException, ParseException {
        List<Depot> depotList = depotRepository.setStockByOrder(dto.getDrugCard());
        Optional<CustomerSupplyOrder> optionalCustomerSupplyOrder = customerSupplyOrderRepository.findById(dto.getCustomerSupplyOrder());
        if (!optionalCustomerSupplyOrder.isPresent()) {
            throw new NotFoundException("Böyle Bir Sipariş Kaydı Yok");
        }

        Optional<CustomerOrder> optionalCustomerOrder = customerOrderRepository.findById(dto.getCustomerOrder());
        if (!optionalCustomerOrder.isPresent()) {
            throw new NotFoundException("Böyle Bir İstek Yok");
        }

        //  Date exp=dto.getExpirationDate();
        String date = new SimpleDateFormat("yyyy-MM-dd").format(dto.getExpirationDate());
        Date exp = new SimpleDateFormat("yyyy-MM-dd").parse(date);
        if (depotList.size() < dto.getLimitation()) {
            throw new NotFoundException("Stokta Yeterli Miktarda İlaç Yok");
        }
        depotList.removeIf(c -> (c.getExpirationDate().before(exp)));
        // for(Depot depot:depotList){
        if (depotList.size() == 0) {
            return false;
        }

        if (dto.getLimitation() == 0) {
            throw new NotFoundException(" 0 Sipariş Verilemez");
        }

        for (int i = 0; i < dto.getLimitation(); i++) {

            depotList.get(i).setDepotStatus(depotStatusRepository.findById(4L).get());//Sipariş için verilen status
            depotList.get(i).setCustomerSupplyOrder(optionalCustomerSupplyOrder.get());
            depotList.get(i).setCustomerOrder(optionalCustomerOrder.get());
            // depotList.get(i).setPurchaseOrderDrugsId(optionalCustomerSupplyOrder.get().getPurchaseOrderDrugs().getPurchaseOrderDrugsId());
            depotRepository.save(depotList.get(i));
            customerSupplyOrderRepository.save(optionalCustomerSupplyOrder.get());
        }

        return true;
    }

    //verilen stoktan sipariş için ilaç ayırma işlemi iptali
    public Boolean reverseOrderByStock(Long customerSupplyOrder) throws NotFoundException {
        Optional<CustomerSupplyOrder> optionalCustomerSupplyOrder = customerSupplyOrderRepository.findById(customerSupplyOrder);
        if (!optionalCustomerSupplyOrder.isPresent()) {
            throw new NotFoundException("Böyle Bir Sipariş Yok");
        }
        List<Depot> depotList = depotRepository.reverseOrderByStock(customerSupplyOrder);
        if (depotList.size() == 0) {
            throw new NotFoundException("Bu Sipariş İçin Stoktan Ayırılan Bir İlaç Yok");
        }

        for (Depot depot : depotList) {
            depot.setDepotStatus(depotStatusRepository.findById(10L).get());//sipariş bozma
            depot.setCustomerSupplyOrder(null);
            depot.setCustomerOrder(null);
            depot = depotRepository.save(depot);
        }
//        Cancel order drug kısmında yaptık
//        optionalCustomerSupplyOrder.get().getPurchaseOrderDrugs().setChargedQuantity( optionalCustomerSupplyOrder.get().getPurchaseOrderDrugs().getChargedQuantity()-depotList.size());
//        optionalCustomerSupplyOrder.get().getPurchaseOrderDrugs().setIncompleteQuantity(optionalCustomerSupplyOrder.get().getPurchaseOrderDrugs().getTotalQuantity()-optionalCustomerSupplyOrder.get().getPurchaseOrderDrugs().getChargedQuantity());
        customerSupplyOrderRepository.save(optionalCustomerSupplyOrder.get());
        return true;
    }

    //Stocktan sipariş için ilaç sayısı çekme
    public Integer getStockByOrder(Long drugCard, DepotDto depotDto) throws NotFoundException {
        List<Depot> depotList = depotRepository.getStockByOrder(drugCard);
        Date exp = depotDto.getExpirationDate();

        depotList.removeIf(c -> (c.getExpirationDate().before(exp)));

        //Tek tek görmek istersek
        //DepotDto[] depotDtos=mapper.map(depotList,DepotDto[].class);
        //List<DepotDto> dtoList = Arrays.asList(depotDtos);
        return depotList.size();
    }

    public Boolean setPurchases(PurchaseOrderDrugs purchaseOrderDrugs) {
        if (purchaseOrderDrugs.getPurchaseStatus().getPurchaseStatusId() == 5L) {
            //İhracat izni gerektrien durum
        } else if (purchaseOrderDrugs.getPurchaseStatus().getPurchaseStatusId() == 7L) {
            //Müdür izni gerektrien durum
        } else if (purchaseOrderDrugs.getChargedQuantity().equals(purchaseOrderDrugs.getTotalQuantity())) {
            purchaseOrderDrugs.setPurchaseStatus(purchaseStatusRepository.findById(40L).get());
        } else if (purchaseOrderDrugs.getChargedQuantity() < purchaseOrderDrugs.getTotalQuantity()) {
            purchaseOrderDrugs.setPurchaseStatus(purchaseStatusRepository.findById(20L).get());
        } else if (purchaseOrderDrugs.getChargedQuantity() == 0) {
            purchaseOrderDrugs.setPurchaseStatus(purchaseStatusRepository.findById(10L).get());
        } else {
            return false;
        }

        return true;
    }

    private User getUserFromToken(String authHeader) throws NotFoundException {

        String username = controlService.getUsernameFromToken(authHeader);
        Optional<User> optUser = userRepository.findByUsername(username);
        if (!optUser.isPresent()) {
            throw new NotFoundException("Böyle Bir Kullanıcı Yok");
        }
        return optUser.get();
    }

    //    public Page<SupplyCustomerListDto> getAllWithPageUser(Integer pageNo, Integer pageSize, String sortBy) throws Exception {
//        try {
//                StringBuilder createSqlQuery = new StringBuilder("select * from customer_order");
//
//                List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Supplier.class).getResultList();
//
//            SupplyCustomerListDto[] dtos = mapper.map(list,SupplyCustomerListDto[].class );
//                List<SupplyCustomerListDto> dtosList=Arrays.asList(dtos);
//
//                Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
//                int start = Math.min((int) paging.getOffset(), dtosList.size());
//                int end = Math.min((start + paging.getPageSize()), dtosList.size());
//
//                Page<SupplyCustomerListDto> pageList = new PageImpl<>(dtosList.subList(start, end), paging, dtosList.size());
//
//                return pageList;
//
//
//        } catch (Exception e) {
//            throw e;
//        }
//
//    }
    public Page<SupplyCustomerListDto> getAllWithPage(String authHeader, Pageable page, SupplyCustomerListDto dto) throws Exception {
        try {

            StringBuilder createSqlQuery = new StringBuilder("select * from customer_order where status=1 and customer_order_id>1");

            if (dto.getCustomerOrderNo() != null) {
                createSqlQuery.append("and  customer_order_no ILIKE '%" + dto.getCustomerOrderNo() + "%' ");
            }

            createSqlQuery.append(" order by order_date");
            List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), CustomerOrder.class).getResultList();

            SupplyCustomerListDto[] dtos = mapper.map(list, SupplyCustomerListDto[].class);
            List<SupplyCustomerListDto> liste = Arrays.asList(dtos);

            int start = Math.min((int) page.getOffset(), liste.size());
            int end = Math.min((start + page.getPageSize()), liste.size());

            Page<SupplyCustomerListDto> pageList = new PageImpl<>(liste.subList(start, end), page, liste.size());

            for (SupplyCustomerListDto scl : liste) {
                scl.setPurchaseStatus(getAllStatus(scl.getCustomerOrderId()));
            }

            return pageList;


        } catch (Exception e) {
            throw e;
        }

    }


    public Boolean updateOrder(CustomerSupplyOrderDto dto) throws NotFoundException {


        Optional<CustomerSupplyOrder> optCSO = customerSupplyOrderRepository.findById(dto.getCustomerSupplyOrderId());
        if (!optCSO.isPresent()) {
            throw new NotFoundException(" Böyle bir sipariş yok");
        }
        if (optCSO.get().getQuantity() < dto.getQuantity()) {
            throw new NotFoundException("Eski Siparişten Daha Fazla Bir Sipariş Adedi Girilemez");
        }

        if (dto.getTotality() > optCSO.get().getTotality())
            if (optCSO.get().getCustomerSupplyStatus().getCustomerSupplyStatusId() != 20L) {
                throw new NotFoundException(" Bu Siparişin Değişim İzni Yoktur");
            }
        //  CustomerSupplyOrder cso = mapper.map(dto, CustomerSupplyOrder.class);

        try {
            Double totp, genp, bavrup, id, dd, sur, unit, sp, qua, mf1, mf2, total;
            String mf;

            //Total qua=total-stock
            //totality =qua+surplusqua

            qua = dto.getQuantity().doubleValue();

            unit = optCSO.get().getUnitPrice().doubleValue();


            dd = optCSO.get().getDistributorDiscount().doubleValue();
            id = optCSO.get().getInstitutionDiscount().doubleValue();
            sp = dto.getSupplierProfit().doubleValue();
            mf = dto.getSurplus();
            mf1 = Double.parseDouble(mf.substring(0, mf.indexOf("-")));
            mf2 = Double.parseDouble(mf.substring(mf.indexOf("-") + 1, mf.length()));


            genp = qua * unit;

            bavrup = unit - (unit * (id / 100));
            // System.out.println("avrup1 " +avrup);
            bavrup -= (bavrup * (dd / 100));
            //  System.out.println("avrup2 " +avrup);
            bavrup += (bavrup * (sp / 100));
            //   System.out.println("avrup3 " +avrup);
            // bavrup += (bavrup * 0.08);// birim başına avarage unit price///KDV İSTER ÜZERİNE KALDIRILDI
            //  System.out.println("avrup4 " +avrup);

            sur = ((qua - (qua % mf1)) / mf1) * mf2;

            total = qua + sur;

            Double averageup = ((bavrup * qua) / total);//son average unit


            Optional<PurchaseOrderDrugs> purchaseOrderDrugs = purchaseOrderDrugsRepository.findById(optCSO.get().getPurchaseOrderDrugs().getPurchaseOrderDrugsId());
            if (!purchaseOrderDrugs.isPresent()) {
                throw new NotFoundException(" Böyle Bir İhracat Siparişi Yok");
            }
            Long stock = 0L;
            purchaseOrderDrugs.get().setChargedQuantity(purchaseOrderDrugs.get().getTotalQuantity() - optCSO.get().getTotalQuantity());
            purchaseOrderDrugs.get().setIncompleteQuantity(purchaseOrderDrugs.get().getTotalQuantity() - purchaseOrderDrugs.get().getChargedQuantity());

            stock = purchaseOrderDrugs.get().getIncompleteQuantity();
            if (stock < total) {
                stock = total.longValue() - stock;
            }
            Double totalqua = total - stock;//////////////


//
//            //Eski siparişi çıkardık
//            purchaseOrderDrugs.get().setChargedQuantity(purchaseOrderDrugs.get().getChargedQuantity()-optCSO.get().getTotality());
//            purchaseOrderDrugs.get().setIncompleteQuantity(purchaseOrderDrugs.get().getTotalQuantity()-purchaseOrderDrugs.get().getChargedQuantity());


            //Yeni siparişi ekledik
            purchaseOrderDrugs.get().setChargedQuantity(purchaseOrderDrugs.get().getChargedQuantity() + totalqua.longValue());
            purchaseOrderDrugs.get().setIncompleteQuantity(purchaseOrderDrugs.get().getTotalQuantity() - purchaseOrderDrugs.get().getChargedQuantity());

            optCSO.get().setSupplierProfit(sp.floatValue());
            optCSO.get().setSurplus(mf);
            optCSO.get().setAverageUnitPrice(averageup.floatValue());
            optCSO.get().setGeneralPrice(genp.longValue());
            optCSO.get().setDistributorDiscount(dd.floatValue());
            optCSO.get().setInstitutionDiscount(id.floatValue());
            optCSO.get().setQuantity(qua.longValue());
            optCSO.get().setStocks(stock);
            optCSO.get().setSurplusQuantity(sur.longValue());
            optCSO.get().setTotalPrice(bavrup * qua);
            optCSO.get().setTotalQuantity(totalqua.longValue());
            optCSO.get().setTotality(total.longValue());
            optCSO.get().setUnitPrice(unit.floatValue());
            optCSO.get().setVat(0F);//KDV iSTER ÜZERİNE KALDIRILDI
            optCSO.get().setCustomerSupplyStatus(customerSupplyStatusRepository.findById(10L).get());


            customerSupplyOrderRepository.save(optCSO.get());

        } catch (Exception e) {
            throw new NotFoundException(" Kaydedilemedi");
        }


        return true;

    }

    public CustomerSuppliersDto getSupplyOrder(Long customerSupplyOrderId) throws NotFoundException {
        Optional<CustomerSupplyOrder> optionalCustomerSupplyOrder = customerSupplyOrderRepository.findById(customerSupplyOrderId);
        if (!optionalCustomerSupplyOrder.isPresent()) {
            throw new NotFoundException("Böyle Bir Sipariş Yok");
        }
        CustomerSuppliersDto customerSuppliersDto = mapper.map(optionalCustomerSupplyOrder.get(), CustomerSuppliersDto.class);


        return customerSuppliersDto;
    }

    public Boolean denemee() throws Exception {

        for (long i = 1; i < 2; i++) {

            CustomerOrder customerOrder = new CustomerOrder();
            customerOrder.setCustomerOrderNo(" 2" + i);
            customerOrder.setOrderDate(createdAt);
            customerOrder.setStatus(1);
            customerOrder.setCustomer(customerRepository.findById(2L).get());
            customerOrder.setUser(userRepository.findByUsername("admin").get());
            customerOrder.setOrderStatus(customerOrderStatusRepository.findById(15L).get());
            if(!customerOrderStatusHistoryService.save(customerOrder,customerOrder.getOrderStatus()))
                throw new Exception("Sipariş Oluşturulamadı");
            customerOrder = customerOrderRepository.save(customerOrder);
            System.out.println(i + " . Customer");

            for (long j = 1; j < 2000; j++) {

                PurchaseOrderDrugs pod = new PurchaseOrderDrugs();
                pod.setChargedQuantity(0L);
                pod.setExpirationDate(createdAt);
                pod.setIncompleteQuantity(1000L);
                pod.setPurchaseOrderDrugNote("Deneme");
                pod.setTotalQuantity(1000L);
                pod.setDrugCard(drugCardRepository.findById(j).get());
                pod.setPurchaseStatus(purchaseStatusRepository.findById(10L).get());
                pod.setCustomerOrder(customerOrder);
                pod = purchaseOrderDrugsRepository.save(pod);
                System.out.println(j + " . İlaç");

            }
        }
        return true;
    }

    public List<CustomerOrderDrugsDto> getCustomerOrderDrugList(Long customerOrderId) throws Exception {
        //Optional<CustomerOrderStatus> customerOrderStatus = orderStatusRepository.findById(10l);
        Optional<CustomerOrder> co = customerOrderRepository.findById(customerOrderId);
        if(!co.isPresent())
            throw new Exception("Müşteri Siparişi Bulunamadı");
        List<CustomerOrderDrugs> drugList = customerOrderDrugsRepository.findByCustomerOrder(co.get());
        CustomerOrderDrugsDto[] array = mapper.map(drugList, CustomerOrderDrugsDto[].class);
        List<CustomerOrderDrugsDto> dtos = Arrays.asList(array);
        dtos.forEach(data ->
                data.setDepotCount(depotRepository.countOfDrugs(data.getCustomerOrder().getCustomerOrderId(),data.getCustomerOrderDrugId())));

        return dtos;
    }
}
