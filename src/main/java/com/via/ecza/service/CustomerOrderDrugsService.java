package com.via.ecza.service;

import com.via.ecza.dto.*;
import com.via.ecza.entity.*;
import com.via.ecza.entity.enumClass.Role;
import com.via.ecza.repo.*;
import javassist.NotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
public class CustomerOrderDrugsService {
    @Autowired
    private CustomerOrderRepository customerOrderRepository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ControlService controlService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private CustomerOrderDrugsRepository customerOrderDrugsRepository;
    @Autowired
    private DrugCardRepository drugCardRepository;
    @Autowired
    private CustomerOrderStatusRepository orderStatusRepository;
    @Autowired
    private CampaignRepository campaignRepository;
    @Autowired
    private DepotRepository depotRepository;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private PurchaseOrderDrugsRepository purchaseOrderDrugsRepository;
    @Autowired
    private CustomerSupplyOrderRepository customerSupplyOrderRepository;
    @Autowired
    private SupplierOfferRepository supplierOfferRepository;

    @Autowired
    private PurchaseStatusRepository purchaseStatusRepository;

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public Boolean save(@Valid CustomerOrderDrugsSaveDto dto) throws Exception {
        try {

            Optional<DrugCard> optDrugCard = drugCardRepository.findById(dto.getDrugCardId());
            if (!optDrugCard.isPresent()) {
                throw new NotFoundException("İlaç  Bulunamadı..");
            }
            Optional<CustomerOrder> optionalCustomerOrder = customerOrderRepository.findById(dto.getCustomerOrderId());
            if (!optionalCustomerOrder.isPresent()) {
                throw new NotFoundException("Yurt dışı sipariş Bulunamadı..");
            }
            CustomerOrder customerOrder = optionalCustomerOrder.get();
            Optional<Customer> optionalCustomer = customerRepository.findById(dto.getCustomerId());
            if (!optionalCustomer.isPresent()) {
                throw new NotFoundException("Müşteri Bulunamadı..");
            }
            if (optionalCustomer.get().getCustomerId() != optionalCustomerOrder.get().getCustomer().getCustomerId()) {
                throw new Exception("Siparişe bağlı müşteri kaydı uyuşmadı");
            }
            //CustomerOrderDrugs customerOrderDrugs = mapper.map(dto, CustomerOrderDrugs.class);
            List<Country> list = null;
            if(optDrugCard.get().getSourceCountryForCalculation() != null) {
                list = countryRepository.findByNameContainingIgnoreCase(optDrugCard.get().getSourceCountryForCalculation());
            }
            else
                list = countryRepository.findByNameContainingIgnoreCase(optDrugCard.get().getSourceCountry());

            CustomerOrderDrugs customerOrderDrugs = new CustomerOrderDrugs();
            customerOrderDrugs.setCreatedDate(new Date());
            customerOrderDrugs.setDrugCard(optDrugCard.get());
            customerOrderDrugs.setExpirationDate(dto.getExpirationDate());
            customerOrderDrugs.setCustomerOrderDrugNote(dto.getCustomerOrderDrugNote());
            customerOrderDrugs.setTotalQuantity(dto.getTotalQuantity());
            if(list.size()>0)
                customerOrderDrugs.setEnglishCountryName(list.get(0).getEnglishName());
            else
                customerOrderDrugs.setEnglishCountryName(optDrugCard.get().getSourceCountryForCalculation());
            customerOrderDrugs.setUnitPrice(Double.valueOf(0));
            customerOrderDrugs.setIsCampaignedDrug(0);
            customerOrderDrugs.setPurchaseOrderStatus(0);
            customerOrderDrugs.setCurrencyFee(1D);
            customerOrderDrugs.setProfit(0);
            customerOrderDrugs.setCurrencyType(customerOrder.getCurrencyType());
            DrugCard drugCard = optDrugCard.get();
            Date currentDate = new Date();

            //Campaign campaign = campaignRepository.controlDate(drugCard.getDrugCardId(), currentDate,currentDate);
            //Campaign campaign = campaignRepository.controlDate(drugCard.getDrugCardId(), currentDate);


            // kampanyalı ilaç kontrolü
            StringBuilder createSqlQuery = new StringBuilder();
            createSqlQuery.append(" select * from campaign c where c.currency_type='"+customerOrder.getCurrencyType()+"' and c.drug_card_id ="+drugCard.getDrugCardId()+"  and c.is_deleted = 0");
            createSqlQuery.append(" and c.campaign_start_date <=  to_date('" + sdf.format(currentDate) + "'," + "'dd.MM.yyyy') ");
            createSqlQuery.append (" and c.campaign_end_date >= to_date('" + sdf.format(currentDate) + "'," + "'dd.MM.yyyy') ");
            List<Object> obj = entityManager.createNativeQuery(createSqlQuery.toString(), Campaign.class).getResultList();
            if(obj.size()>0){
                Campaign campaign  = (Campaign) obj.get(0);
                if (campaign != null) {
                    customerOrderDrugs.setCampaign(campaign);
                    customerOrderDrugs.setCurrencyFee(campaign.getCurrencyFee());
                    customerOrderDrugs.setUnitPrice(campaign.getCampaignUnitPrice());
                    customerOrderDrugs.setProfit(campaign.getProfit());
                    customerOrderDrugs.setSurplusOfGoods1(campaign.getMf1());
                    customerOrderDrugs.setSurplusOfGoods2(campaign.getMf2());
                    customerOrderDrugs.setIsCampaignedDrug(1);
                }
            }

            customerOrderDrugs.setDrugCard(drugCard);
            customerOrderDrugs.setCurrency("");
            customerOrderDrugs.setChargedQuantity(0L);
            customerOrderDrugs.setUnitCost((double) 0);
            customerOrderDrugs.setIsDeleted(0);
            customerOrderDrugs.setIsAddedByManager(0);
            customerOrderDrugs.setIncompleteQuantity(customerOrderDrugs.getTotalQuantity() - customerOrderDrugs.getChargedQuantity());

            //customerOrderDrugs.setCustomerOrder(optionalCustomerOrder.get());


            if (!(customerOrderDrugs.getUnitPrice() > 0)) {
                Double bf = customerOrderDrugs.getDrugCard().getPrice().getDepotSalePriceExcludingVat();
                Long q = customerOrderDrugs.getTotalQuantity();
                Float ti = Float.valueOf(0);
                if (customerOrderDrugs.getDrugCard().getDiscount() != null) {
                    if (customerOrderDrugs.getDrugCard().getDiscount().getInstutionDiscount() > 0)
                        ti = customerOrderDrugs.getDrugCard().getDiscount().getInstutionDiscount() / 100;
                }

                Double result1 = (bf - (bf * ti));

                double unitCost = ((double) ((int) (result1 * 100.0))) / 100.0;
                customerOrderDrugs.setSurplusOfGoods1(1);   //mf1
                customerOrderDrugs.setSurplusOfGoods2(0);   //mf2
                customerOrderDrugs.setUnitCost(unitCost);
                customerOrderDrugs.setInstutionDiscount(ti * 100);
                customerOrderDrugs.setGeneralDiscount(0f);
                //customerOrderDrugs.setUnitPrice(null);
            }


            customerOrderDrugs.setCustomerOrder(customerOrder);
            customerOrderDrugs = customerOrderDrugsRepository.save(customerOrderDrugs);
            customerOrder.getCustomerOrderDrugs().add(customerOrderDrugs);
            customerOrder = customerOrderRepository.save(customerOrder);
            return true;

            // exporter
            // customerorderid   cusrtomerid  userid
        } catch (Exception e) {
            throw e;
        }
    }

    public String setPrices(List<CustomerOrderDrugSetPricesDto> dtoList) throws Exception {
//        System.out.println(dtoList.size());
        String result = "";
        Boolean control = true;
        try {
            if(dtoList.size()<1){
                result= "Boş Alanları Doldurunuz.";
                control=false;
            }
            for (CustomerOrderDrugSetPricesDto dto : dtoList) {
                Optional<CustomerOrderDrugs> opt = customerOrderDrugsRepository.findById(dto.getCustomerOrderDrugId());
                if (opt.isPresent()) {
                    CustomerOrderDrugs drugs = opt.get();

                    //if(dto.getUnitPrice()>0) drugs.setUnitPrice(dto.getUnitPrice());
//                    Double cost = drugs.getUnitCost();
//                    if (dto.getUnitCost() != null)
//                        cost = dto.getUnitCost();

                    Float ti = Float.valueOf(0);
                    if (drugs.getDrugCard().getDiscount() != null) {
                        if (drugs.getDrugCard().getDiscount().getInstutionDiscount() > 0)
                            ti = drugs.getDrugCard().getDiscount().getInstutionDiscount() / 100;
                    }

                    Float ki = Float.valueOf(0);
//                    if (drugs.getDrugCard().getDiscount() != null) {
//                        if (drugs.getDrugCard().getDiscount().getGeneralDiscount() > 0)
//                            ki = drugs.getDrugCard().getDiscount().getGeneralDiscount() / 100;
//                    }
                    Integer mf1 = 1, mf2 = 0;

                    if (dto.getSurplusOfGoods1() != null)   mf1 = Integer.valueOf(dto.getSurplusOfGoods1());
                    else if( drugs.getSurplusOfGoods1()>0)  mf1 = drugs.getSurplusOfGoods1();

                    if (dto.getSurplusOfGoods2() != null)   mf2 = Integer.valueOf(dto.getSurplusOfGoods2());
                    else if( drugs.getSurplusOfGoods1()>=0) mf2 = drugs.getSurplusOfGoods2();

                    Double output =0D, resultMf = 0D;

                    Double bf = drugs.getDrugCard().getPrice().getDepotSalePriceExcludingVat();
                    // iskontolu birim fiyat
                    bf =(double) (bf - (bf * ti));

                    // mal mazlası oranı
                    resultMf =  ( (double) mf1/ (double)(mf1+mf2));

                    // mal fazlalı birim fiyat
                    output = bf*resultMf;

                    // kdv lı ve mal fazlalı birim fiyat
                    if(drugs.getDrugCard().getDrugVat() != null ){
                        output = output + output*drugs.getDrugCard().getDrugVat()/100;
                    }

                    Double unitCost = ((double) ((int) (output * 1000.0))) / 1000.0;

                    //eğer yurt dışı fiyatı manuel belirlenmiş ise otomatik onu alır
                    //aksi durumda marj vs hesaplanıp bulunur
                    Double unitPrice=0D;
                   if(dto.getUnitPrice()!=null){
                       unitPrice=dto.getUnitPrice();
                   }else{
                       unitPrice = unitCost + (unitCost * dto.getProfit() / 100);
                   }




                    if (drugs.getUnitPrice() ==  0 && Math.round(unitPrice) > 0) {

                        if (dto.getCurrency() != null)
                            drugs.setCurrency(dto.getCurrency());

                        drugs.setSurplusOfGoods1(mf1);
                        drugs.setSurplusOfGoods2(mf2);
                        drugs.setUnitCost(unitCost);
                        drugs.setUnitPrice(unitPrice);
                        drugs.setInstutionDiscount(ti);
                        drugs.setGeneralDiscount(ki);
                        drugs.setProfit(dto.getProfit());
                        drugs.setDrugVat(drugs.getDrugCard().getDrugVat());
                        drugs.setDepotSalePriceExcludingVat(drugs.getDrugCard().getPrice().getDepotSalePriceExcludingVat());
                        drugs.setCurrencyType(drugs.getCustomerOrder().getCurrencyType());
                        drugs.setCurrencyFee(drugs.getCustomerOrder().getCurrencyFee());
                        drugs = customerOrderDrugsRepository.save(drugs);
                    } else {
                        result= "Birim satış fiyatı 0 dan büyük olmalıdır.";
                        control = false;
                    }

                }
            }

        } catch (Exception e) {
            throw e;
        }
        return result;
    }

    public List<CustomerOrderDrugsListDto> search(CustomerOrderDrugsSearchDto dto) {
        StringBuilder createSqlQuery = new StringBuilder("Select * from customer_order_drugs cod ");
        createSqlQuery.append("join customer_order co ON co.customer_order_id =cod.customer_order_id ");
        createSqlQuery.append("where co.status =1 ");

        if (dto.getDrugCardId() != null) createSqlQuery.append(" and cod.drug_card_id  = " + dto.getDrugCardId() + " ");

        if (dto.getOrderStatusId() != null && dto.getOrderStatusId() != 0) {
            createSqlQuery.append("and  co.order_status_id = " + dto.getOrderStatusId() + " ");
        }
//        else{
//            createSqlQuery.append(" and co.order_status_id = 2");
//        }
        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), CustomerOrderDrugs.class).getResultList();

        CustomerOrderDrugsListDto[] dtos = mapper.map(list, CustomerOrderDrugsListDto[].class);

        return Arrays.asList(dtos);
    }

    public Boolean delete(String authHeader, CustomerOrderDrugDeleteDto dto) throws Exception {
        User user = controlService.getUserFromToken(authHeader);

        if(user.getRole() == Role.ADMIN){
            Optional<CustomerOrder> optionalCustomerOrder = customerOrderRepository.findById(dto.getCustomerOrderId());
            if (!optionalCustomerOrder.isPresent()) {
                throw new Exception("Böyle bir sipariş yoktur.");
            }
            Optional<CustomerOrderDrugs> optionalCustomerOrderDrugs = customerOrderDrugsRepository.findByCustomerOrderDrugIdAndCustomerOrder(dto.getCustomerOrderDrugId(), optionalCustomerOrder.get());
            if (!optionalCustomerOrderDrugs.isPresent()) {
                return false;
            }
            CustomerOrderDrugs customerOrderDrugs = optionalCustomerOrderDrugs.get();

            if(customerOrderDrugs.getPurchaseOrderDrugsId() != null){
                PurchaseOrderDrugs purchaseOrderDrugs= null;
                Optional<PurchaseOrderDrugs> opt = purchaseOrderDrugsRepository.findById(customerOrderDrugs.getPurchaseOrderDrugsId());
                if(opt.isPresent()){
                    purchaseOrderDrugs = opt.get();
                    Optional<CustomerSupplyOrder> optionalCustomerSupplyOrder = customerSupplyOrderRepository.findByPurchaseOrderDrugs(purchaseOrderDrugs);
                    if (!optionalCustomerSupplyOrder.isPresent()){
                        List<SupplierOffer> list = supplierOfferRepository.findByPurchaseOrderDrugs(purchaseOrderDrugs);
                        int control = 0;

                        for (SupplierOffer so: list)
                            if(so.getSupplierOfferStatus().getSupplierOfferStatusId() != 7 ||
                                    so.getSupplierOfferStatus().getSupplierOfferStatusId() != 45)  control++;


                        if(control == list.size()){
                            for (SupplierOffer so: list) {
                                so.setDrugCard(null);
                                supplierOfferRepository.delete(so);
                            }

                        }else return false;

                        purchaseOrderDrugs.setDrugCard(null);
                        //purchaseOrderDrugs = purchaseOrderDrugsRepository.save(purchaseOrderDrugs);
                        purchaseOrderDrugsRepository.deleteById(purchaseOrderDrugs.getPurchaseOrderDrugsId());

                    }
                    else
                        return false;
                }
            }
            customerOrderDrugs.setDrugCard(null);
            //customerOrderDrugs= customerOrderDrugsRepository.save(customerOrderDrugs);
            customerOrderDrugsRepository.deleteById(customerOrderDrugs.getCustomerOrderDrugId());
            return true;
        } else if(user.getRole() == Role.EXPORTER){

            Optional<CustomerOrder> optionalCustomerOrder = customerOrderRepository.findById(dto.getCustomerOrderId());
            if (!optionalCustomerOrder.isPresent()) {
                throw new Exception("Böyle bir sipariş yoktur.");
            }
            Optional<CustomerOrderDrugs> optionalCustomerOrderDrugs = customerOrderDrugsRepository.findByCustomerOrderDrugIdAndCustomerOrder(dto.getCustomerOrderDrugId(), optionalCustomerOrder.get());
            if (!optionalCustomerOrderDrugs.isPresent()) {
                return false;
            }
            CustomerOrderDrugs customerOrderDrugs = optionalCustomerOrderDrugs.get();
            if (customerOrderDrugs.getCustomerOrder().getOrderStatus().getOrderStatusId() > 14 &&  customerOrderDrugs.getPurchaseOrderStatus() == 1)
                return false;
            customerOrderDrugs.setDrugCard(null);
            customerOrderDrugsRepository.delete(customerOrderDrugs);
            return true;
        }
        return false;
    }

    public Boolean update(String authHeader, CustomerOrderDrugUpdateDto dto) throws Exception {
        User user = this.getUserFromToken(authHeader);
        if (user.getRole() == Role.ADMIN) {
            Optional<DrugCard> optDrugCard = drugCardRepository.findById(dto.getDrugCardId());
            if (!optDrugCard.isPresent()) {
                throw new NotFoundException("İlaç  Bulunamadı..");
            }
            Optional<CustomerOrder> optionalCustomerOrder = customerOrderRepository.findById(dto.getCustomerOrderId());
            if (!optionalCustomerOrder.isPresent()) {
                throw new NotFoundException("Yurt dışı sipariş Bulunamadı..");
            }
            Optional<CustomerOrderDrugs> optional = customerOrderDrugsRepository.findById(dto.getCustomerOrderDrugId());
            CustomerOrderDrugs customerOrderDrugs = optional.get();
            if (customerOrderDrugs.getCustomerOrder().getOrderStatus().getOrderStatusId() > 14) {
                Optional<PurchaseOrderDrugs> optionalPurchaseOrderDrugs = purchaseOrderDrugsRepository.getOneForExporter(dto.getCustomerOrderDrugId());
                if (!optionalPurchaseOrderDrugs.isPresent()) {
                    throw new NotFoundException("Yurtiçi Satın Alma Kaydı Bulunamadı");
                }
                customerOrderDrugs.setFreightCostTl(dto.getFreightCostTl());
                customerOrderDrugs = customerOrderDrugsRepository.save(customerOrderDrugs);
                PurchaseOrderDrugs purchaseOrderDrugs = optionalPurchaseOrderDrugs.get();
                if (customerOrderDrugs.getExpirationDate().after(dto.getExpirationDate())) {
                    purchaseOrderDrugs.setExpirationDate(dto.getExpirationDate());
                    customerOrderDrugs.setExpirationDate(dto.getExpirationDate());
                    customerOrderDrugs = customerOrderDrugsRepository.save(customerOrderDrugs);
                }
//                else{
//                    throw new Exception("Son Kullanma Tarihi ileri Bir Tarih Seçilemez");
//                }

                purchaseOrderDrugs.setPurchaseOrderDrugExportNote(dto.getCustomerOrderDrugNote());
                purchaseOrderDrugs = purchaseOrderDrugsRepository.save(purchaseOrderDrugs);
                return true;
            }

            customerOrderDrugs.setFreightCostTl(dto.getFreightCostTl());
            customerOrderDrugs.setExpirationDate(dto.getExpirationDate());
            DrugCard drugCard = optDrugCard.get();
            customerOrderDrugs.setDrugCard(drugCard);
            if (customerOrderDrugs.getCustomerOrder().getOrderStatus().getOrderStatusId() < 15)
                customerOrderDrugs.setTotalQuantity(dto.getTotalQuantity());
            customerOrderDrugs.setCustomerOrderDrugNote(dto.getCustomerOrderDrugNote());
            customerOrderDrugs.setChargedQuantity(0L);
            customerOrderDrugs.setIncompleteQuantity(customerOrderDrugs.getTotalQuantity() - customerOrderDrugs.getChargedQuantity());

            customerOrderDrugs = customerOrderDrugsRepository.save(customerOrderDrugs);
            return true;
        } else if (user.getRole() == Role.EXPORTER) {
            Optional<DrugCard> optDrugCard = drugCardRepository.findById(dto.getDrugCardId());
            if (!optDrugCard.isPresent()) {
                throw new NotFoundException("İlaç  Bulunamadı..");
            }
            Optional<CustomerOrder> optionalCustomerOrder = customerOrderRepository.findById(dto.getCustomerOrderId());
            if (!optionalCustomerOrder.isPresent()) {
                throw new NotFoundException("Yurt dışı sipariş Bulunamadı..");
            }
            if (optionalCustomerOrder.get().getUser().getUserId() != user.getUserId()) {
                throw new Exception("Müşteri sipariş kaydı size ait değildir.");
            }
            Optional<CustomerOrderDrugs> optional = customerOrderDrugsRepository.findById(dto.getCustomerOrderDrugId());
            CustomerOrderDrugs customerOrderDrugs = optional.get();
            if (customerOrderDrugs.getCustomerOrder().getOrderStatus().getOrderStatusId() > 14) {
                Optional<PurchaseOrderDrugs> optionalPurchaseOrderDrugs = purchaseOrderDrugsRepository.getOneForExporter(dto.getCustomerOrderDrugId());
                if (!optionalPurchaseOrderDrugs.isPresent()) {
                    throw new NotFoundException("Yurtiçi Satın Alma Kaydı Bulunamadı");
                }
                customerOrderDrugs.setFreightCostTl(dto.getFreightCostTl());
                customerOrderDrugs = customerOrderDrugsRepository.save(customerOrderDrugs);
                PurchaseOrderDrugs purchaseOrderDrugs = optionalPurchaseOrderDrugs.get();
                if (customerOrderDrugs.getExpirationDate().after(dto.getExpirationDate())) {
                    purchaseOrderDrugs.setExpirationDate(dto.getExpirationDate());
                    customerOrderDrugs.setExpirationDate(dto.getExpirationDate());
                    customerOrderDrugs = customerOrderDrugsRepository.save(customerOrderDrugs);
                }
//                else{
//                    throw new Exception("Son Kullanma Tarihi ileri Bir Tarih Seçilemez");
//                }

                purchaseOrderDrugs.setPurchaseOrderDrugExportNote(dto.getCustomerOrderDrugNote());
                purchaseOrderDrugs = purchaseOrderDrugsRepository.save(purchaseOrderDrugs);
                return true;
            }

            DrugCard drugCard = optDrugCard.get();

            customerOrderDrugs.setFreightCostTl(dto.getFreightCostTl());
            customerOrderDrugs.setDrugCard(drugCard);
            customerOrderDrugs.setTotalQuantity(dto.getTotalQuantity());
            customerOrderDrugs.setExpirationDate(dto.getExpirationDate());
            customerOrderDrugs = customerOrderDrugsRepository.save(customerOrderDrugs);
            return true;
        } else {
            return false;
        }
    }


    private User getUserFromToken(String authHeader) throws NotFoundException {

        String username = controlService.getUsernameFromToken(authHeader);
        Optional<User> optUser = userRepository.findByUsername(username);
        if (!optUser.isPresent()) {
            throw new NotFoundException("Not found User");
        }
        return optUser.get();
    }

    public CustomerOrderDrugsSingleDto findById(Long customerOrderDrugId) throws Exception {
        Optional<CustomerOrderDrugs> opt = customerOrderDrugsRepository.findById(customerOrderDrugId);
        if (!opt.isPresent())
            throw new Exception("İlaç kaydı bulunamadı..");
        CustomerOrderDrugsSingleDto dto = mapper.map(opt.get(), CustomerOrderDrugsSingleDto.class);
        return dto;
    }

    public Boolean resetPrices(CustomerOrderDrugSetPricesDto dto) throws Exception {
        Optional<CustomerOrderDrugs> opt = customerOrderDrugsRepository.findById(dto.getCustomerOrderDrugId());
        if (!opt.isPresent())
            throw new Exception("İlaç kaydı bulunamadı..");

        CustomerOrderDrugs drugs = opt.get();
        if(drugs.getIsDeleted() == 1 || drugs.getPurchaseOrderStatus() == 1)
            return false;
//        drugs.setSurplusOfGoods1(0);   //y
//        drugs.setSurplusOfGoods2(0);   //x
        drugs.setUnitCost(null);
        drugs.setUnitPrice(0D);
        drugs.setInstutionDiscount(0F);
        drugs.setGeneralDiscount(0F);
        drugs.setDepotSalePriceExcludingVat(0D);
        drugs.setDrugVat(0D);
        drugs.setIsCampaignedDrug(0);
        drugs.setCampaign(null);
//        drugs.setOverLay(0);
        drugs = customerOrderDrugsRepository.save(drugs);


        return true;
    }

    public Boolean deleteCustomerOrderDrug(String authHeader, CustomerOrderDrugDeleteDto dto) throws Exception {

        User user = this.getUserFromToken(authHeader);
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.MANAGER)  {

            Optional<CustomerOrderDrugs> optionalCod = customerOrderDrugsRepository.getSingleDrugForManager(dto.getCustomerId(), dto.getCustomerOrderId(), dto.getCustomerOrderDrugId());
            if(!optionalCod.isPresent())
                throw new Exception("İlaç Kaydı Bulunamadı");
            CustomerOrderDrugs customerOrderDrugs = optionalCod.get();
            if(customerOrderDrugs.getIsAddedByManager() == 0){
                customerOrderDrugs.setIsDeleted(1);
                customerOrderDrugs = customerOrderDrugsRepository.save(customerOrderDrugs);
            } else{
                customerOrderDrugs.setDrugCard(null);
                customerOrderDrugsRepository.delete(customerOrderDrugs);
            }
        }
        return true;
    }

    public Boolean saveByManager(@Valid CustomerOrderDrugsSaveDto dto) throws Exception {
        try {
            Optional<DrugCard> optDrugCard = drugCardRepository.findById(dto.getDrugCardId());
            if (!optDrugCard.isPresent()) {
                throw new NotFoundException("İlaç  Bulunamadı..");
            }
            Optional<CustomerOrder> optionalCustomerOrder = customerOrderRepository.findById(dto.getCustomerOrderId());
            if (!optionalCustomerOrder.isPresent()) {
                throw new NotFoundException("Yurt dışı sipariş Bulunamadı..");
            }
            Optional<Customer> optionalCustomer = customerRepository.findById(dto.getCustomerId());
            if (!optionalCustomer.isPresent()) {
                throw new NotFoundException("Müşteri Bulunamadı..");
            }
            if (optionalCustomer.get().getCustomerId() != optionalCustomerOrder.get().getCustomer().getCustomerId()) {
                throw new Exception("Siparişe bağlı müşteri kaydı uyuşmadı");
            }
            CustomerOrder customerOrder = optionalCustomerOrder.get();
            CustomerOrderDrugs customerOrderDrugs = mapper.map(dto, CustomerOrderDrugs.class);
            customerOrderDrugs.setUnitPrice(Double.valueOf(0));
            customerOrderDrugs.setIsCampaignedDrug(0);
            customerOrderDrugs.setProfit(0);
            customerOrderDrugs.setCurrencyFee(customerOrder.getCurrencyFee());
            customerOrderDrugs.setCurrencyType(customerOrder.getCurrencyType());
            customerOrderDrugs.setPurchaseOrderStatus(0);
            DrugCard drugCard = optDrugCard.get();
            Date currentDate = new Date();
            customerOrderDrugs.setDrugCard(drugCard);
            customerOrderDrugs.setCurrency("");
            customerOrderDrugs.setChargedQuantity(0L);
            customerOrderDrugs.setUnitCost((double) 0);
            customerOrderDrugs.setIsAddedByManager(1);
            customerOrderDrugs.setIsDeleted(0);
            customerOrderDrugs.setIncompleteQuantity(customerOrderDrugs.getTotalQuantity() - customerOrderDrugs.getChargedQuantity());
            customerOrderDrugs.setCustomerOrder(optionalCustomerOrder.get());

            //Campaign campaign = campaignRepository.controlDate(drugCard.getDrugCardId(), currentDate,currentDate);
            //Campaign campaign = campaignRepository.controlDate(drugCard.getDrugCardId(), currentDate);

            // kampanyalı ilaç kontrolü
            StringBuilder createSqlQuery = new StringBuilder();
            createSqlQuery.append(" select * from campaign c where c.currency_type='"+customerOrder.getCurrencyType()+"' and c.drug_card_id ="+drugCard.getDrugCardId()+"  and c.is_deleted = 0");
            createSqlQuery.append(" and c.campaign_start_date <=  to_date('" + sdf.format(currentDate) + "'," + "'dd.MM.yyyy') ");
            createSqlQuery.append (" and c.campaign_end_date >= to_date('" + sdf.format(currentDate) + "'," + "'dd.MM.yyyy') ");
            List<Object> obj = entityManager.createNativeQuery(createSqlQuery.toString(), Campaign.class).getResultList();
            if(obj.size()>0){
                Campaign campaign  = (Campaign) obj.get(0);
                if (campaign != null) {
                    customerOrderDrugs.setCampaign(campaign);
                    customerOrderDrugs.setCurrencyFee(campaign.getCurrencyFee());
                    customerOrderDrugs.setProfit(campaign.getProfit());
                    customerOrderDrugs.setUnitPrice(campaign.getCampaignUnitPrice());
                    customerOrderDrugs.setSurplusOfGoods1(campaign.getMf1());
                    customerOrderDrugs.setSurplusOfGoods2(campaign.getMf2());
                    customerOrderDrugs.setIsCampaignedDrug(1);
                }
            }


            if (!(customerOrderDrugs.getUnitPrice() > 0)) {
                Double bf = customerOrderDrugs.getDrugCard().getPrice().getDepotSalePriceExcludingVat();
                Long q = customerOrderDrugs.getTotalQuantity();
                Float ti = Float.valueOf(0);
                if (customerOrderDrugs.getDrugCard().getDiscount() != null) {
                    if (customerOrderDrugs.getDrugCard().getDiscount().getInstutionDiscount() > 0)
                        ti = customerOrderDrugs.getDrugCard().getDiscount().getInstutionDiscount() / 100;
                }

                Double result1 = (bf - (bf * ti));

                double unitCost = ((double) ((int) (result1 * 100.0))) / 100.0;
                customerOrderDrugs.setSurplusOfGoods1(1);   //mf1
                customerOrderDrugs.setSurplusOfGoods2(0);   //mf2
                customerOrderDrugs.setUnitCost(unitCost);
                customerOrderDrugs.setInstutionDiscount(ti * 100);
                customerOrderDrugs.setGeneralDiscount(0f);
            }
            customerOrderDrugs = customerOrderDrugsRepository.save(customerOrderDrugs);


            return true;
        } catch (Exception e) {
            throw e;
        }
    }

    public Boolean savePurchaseOrderNote(CustomerOrderDrugNoteSaveDto dto) throws Exception {

        Optional<CustomerOrderDrugs> opt = customerOrderDrugsRepository.findById(dto.getCustomerOrderDrugId());
        if (!opt.isPresent())       throw new Exception("İlaç kaydı bulunamadı..");

        CustomerOrderDrugs orderDrug = opt.get();
        orderDrug.setPurchaseOrderDrugAdminNote(dto.getPurchaseOrderDrugAdminNote());
        orderDrug = customerOrderDrugsRepository.save(orderDrug);
        return true;
    }

    public Boolean startPurchaseAgain(Long customerOrderDrugId) throws Exception {

        Optional<PurchaseOrderDrugs> opt = purchaseOrderDrugsRepository.getPurchasewitStatus5(customerOrderDrugId);
        if (!opt.isPresent())       throw new NotFoundException("Satın Alma kaydı bulunamadı");

        Optional<PurchaseStatus> status = purchaseStatusRepository.findById(10l);
        if (!status.isPresent())       throw new NotFoundException("Satın alma statüs kaydı bulunamadı..");
        PurchaseOrderDrugs purchaseOrderDrugs =  opt.get();
        purchaseOrderDrugs.setPurchaseStatus(status.get());
        purchaseOrderDrugs = purchaseOrderDrugsRepository.save(purchaseOrderDrugs);
        return true ;
    }

    public Boolean controlPurchaseStatus(Long customerOrderDrugId)  throws Exception  {
        Boolean control = true;

        Optional<PurchaseOrderDrugs> opt = purchaseOrderDrugsRepository.getPurchaseForControllingStatus(customerOrderDrugId);
        if (!opt.isPresent())       throw new NotFoundException("Satın Alma kaydı bulunamadı");

        PurchaseOrderDrugs purchaseOrderDrugs =  opt.get();

        if(purchaseOrderDrugs.getPurchaseStatus().getPurchaseStatusId() != 5 )
            control= false;
        return control;
    }

    public Integer showChargedDrugQuantity(CustomerOrderDrugChargedDto dto) throws Exception{
        int depotCount = depotRepository.countOfDrugs(dto.getCustomerOrderId(), dto.getCustomerOrderDrugId());
        return depotCount;
    }

    public List<CustomerOrderDrugsListDto> getCustomerOrderDrugs(String authHeader, Long customerId, Long customerOrderId) throws Exception {

            List<CustomerOrderDrugsListDto> dtoList  = null;
            List<CustomerOrderDrugs> drugsList = null;
            Optional<CustomerOrder> optionalCustomerOrder = null;
            Optional<Customer> optionalCustomer= null;
            User user = this.getUserFromToken(authHeader);
            if (user.getRole() == Role.ADMIN || user.getRole() == Role.MANAGER) {
                optionalCustomer = customerRepository.findById(customerId);
                if (!optionalCustomer.isPresent())          throw new Exception("Müşteri kaydı bulunamadı");

                drugsList = customerOrderDrugsRepository.getAllCustomerOrderDrugsForAdmin(customerOrderId, optionalCustomer.get().getCustomerId());
                if (!(drugsList.size()>0))     throw new Exception("Sipariş kaydı bulunamadı");

            } else if (user.getRole() == Role.EXPORTER) {
                optionalCustomer = customerRepository.findById(customerId);
                if (!optionalCustomer.isPresent())          throw new Exception("Müşteri kaydı bulunamadı");

                drugsList = customerOrderDrugsRepository.getAllCustomerOrderDrugsForExporter(
                        customerOrderId,
                        optionalCustomer.get().getCustomerId(),
                        user.getUserId());
                if (!(drugsList.size()>0))      throw new Exception("Sipariş kaydı bulunamadı");

            }

            CustomerOrderDrugsListDto[] dtos = mapper.map(drugsList,CustomerOrderDrugsListDto[].class );
            List<CustomerOrderDrugsListDto> dtosList = Arrays.asList(dtos);
            dtosList.forEach(data ->
                  data.setDepotCount(depotRepository.countOfDrugs(data.getCustomerOrder().getCustomerOrderId(),data.getCustomerOrderDrugId())));

            return dtosList;
    }

    public Boolean resetDrugs(Long customerOrderId) throws Exception {
        Optional<CustomerOrder> optionalCustomerOrder = customerOrderRepository.findById(customerOrderId);
        if (!optionalCustomerOrder.isPresent())       throw new NotFoundException("Sipariş Kaydı bulunamadı");
        CustomerOrder customerOrder = optionalCustomerOrder.get();

        for(CustomerOrderDrugs drug : customerOrder.getCustomerOrderDrugs()){
            if(drug.getPurchaseOrderStatus() == 1 || drug.getIsDeleted() == 1)
                continue;
            CustomerOrderDrugSetPricesDto dto = new CustomerOrderDrugSetPricesDto();
            dto.setCustomerOrderDrugId(drug.getCustomerOrderDrugId());
            this.resetPrices(dto);
        }
        return true;
    }
}
