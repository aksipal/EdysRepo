package com.via.ecza.service;

import com.via.ecza.dto.*;
import com.via.ecza.entity.*;
import com.via.ecza.repo.*;
import javassist.NotFoundException;

import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.text.ParseException;

import java.text.SimpleDateFormat;
import java.util.*;


@Service
@Transactional
public class RefundOfferService {

    @Autowired
    private ModelMapper mapper;
    @Autowired
    private RefundOfferRepository refundOfferRepository;
    @Autowired
    private DrugCardRepository drugCardRepository;
    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private DepotRepository depotRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private DepotStatusRepository depotStatusRepository;
    @Autowired
    private RefundOfferStatusRepository refundOfferStatusRepository;
    @Autowired
    private RefundStatusRepository refundStatusRepository;
    @Autowired
    private RefundRepository refundRepository;
    @Autowired
    ControlService controlService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CustomerSupplyOrderRepository customerSupplyOrderRepository;

    Date createdAt = new Date(System.currentTimeMillis());

    public List<RefundOfferDto> search(RefundOfferSearchDto dto) throws NotFoundException {

        StringBuilder createSqlQuery = new StringBuilder("select * from refund_offer where refund_offer_status_id between 9 and 49 ");

        if (dto.getDrugCard() != null) {
            createSqlQuery.append("and drug_card_id=" + dto.getDrugCard() + " ");
        }

        if (dto.getSupplier() != null) {
            createSqlQuery.append(" and supplier_id=" + dto.getSupplier() + " ");
        }

        if (dto.getRefundOfferStatus() != null) {
            createSqlQuery.append(" and refund_offer_status_id=" + dto.getRefundOfferStatus() + " ");
        }

        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), RefundOffer.class).getResultList();
        RefundOfferDto[] dtos = mapper.map(list, RefundOfferDto[].class);

        List<RefundOfferDto> liste = Arrays.asList(dtos);


        return liste;
    }

    public List<RefundOfferStatus> getAllRefundOfferStatus() throws NotFoundException {
        List<RefundOfferStatus> list = refundOfferStatusRepository.getAllRefundOfferStatus();
        return list;
    }


    public Boolean saveOffer(RefundOfferSaveDto dto) throws NotFoundException {
        Optional<DrugCard> optionalDrugCard = drugCardRepository.findById(dto.getDrugCard());
        if (!optionalDrugCard.isPresent()) {
            throw new NotFoundException("böyle bir ilaç yok");
        }
        Optional<Supplier> optionalSupplier = supplierRepository.findById(dto.getSupplier());
        if (!optionalSupplier.isPresent()) {
            throw new NotFoundException("böyle bir tedarikçi yok");
        }


        List<Depot> depotList = depotRepository.getStockByOrder(dto.getDrugCard());
        Date exp = dto.getExpirationDate();

        //5 Başka iade talebi sil
        depotList.removeIf(c -> (c.getDepotStatus().getDepotStatusId() == 5));
        //önce tarihli ilaçları sil//burayı repoya taşıyabilirim.
        depotList.removeIf(c -> (c.getExpirationDate().before(exp)));

        if (depotList.size() < dto.getTotality()) {
            throw new NotFoundException("İstenilen adet ilaç stokta boş durumda bulunmuyor");
        }

        dto.setOfferedTotality(dto.getTotality());
        dto.setOfferedTotalPrice(dto.getTotalPrice());
        RefundOffer refundOffer = mapper.map(dto, RefundOffer.class);


        if (dto.getTotality() > 0 && dto.getTotalPrice() > 0) {
            refundOffer.setDrugCard(optionalDrugCard.get());
            refundOffer.setSupplier(optionalSupplier.get());
            refundOffer = refundOfferRepository.save(refundOffer);
            System.out.println("İade talebi olutuşruldu fakat mail atılmadı !!");
            return true;
            //Mail Atılma durumu eklenecek
        }

        return false;
    }


    //İlaç iadesi için stokta bulunan ilaçları tarihe göre gruplama
    public List<SupplyDrugsByDateDto> getCountByDrug(Long drugCard) throws NotFoundException, ParseException {

        StringBuilder createSqlQuery = new StringBuilder("SELECT d.drug_card_id ,d.expiration_date , count(*)  FROM depot d WHERE d.depot_status_id=10 and d.drug_card_id=" + drugCard + "  GROUP BY d.expiration_date,d.drug_card_id");
        List<Object> list2 = entityManager.createNativeQuery(createSqlQuery.toString()).getResultList();

//        DenemesDto[] dtos = mapper.map(list2,DenemesDto[].class );
//        List<DenemesDto> dtosList = Arrays.asList(dtos);
//
//        System.out.println(dtosList.toString());


        if (list2.size() == 0) {
            return null;
        }
        List<SupplyDrugsByDateDto> dto = new ArrayList<>();

        for (int i = 0; i < list2.size(); i++) {

            StringBuilder sb = new StringBuilder(JSONObject.valueToString(list2.get(i)));

            System.out.println(sb);

            //drugCard
            // sb.append(sb.substring(1,sb.indexOf(",")));
            String drug = sb.substring(1, sb.indexOf(","));


            //expirationDate
            String date = sb.substring(sb.indexOf(",") + 2, sb.indexOf(",") + 12);
            Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(date);

            //count(*)
            String count = sb.substring(sb.lastIndexOf(",") + 1, sb.lastIndexOf("]"));

            SupplyDrugsByDateDto supplyDrugsByDateDto = new SupplyDrugsByDateDto();
            supplyDrugsByDateDto.setDrugCard(drugCard.longValue());
            supplyDrugsByDateDto.setExpirationDate(date1);
            supplyDrugsByDateDto.setCount(Long.parseLong(count));

//            System.out.println("Count "+supplyDrugsByDateDto.getCount());
//            System.out.println("Date "+supplyDrugsByDateDto.getExpirationDate());
//            System.out.println("Drug  "+supplyDrugsByDateDto.getDrugCard());


            dto.add(supplyDrugsByDateDto);

        }


        System.out.println(dto.toString());
        // stoksiparisiver();
        return dto;
    }

    public List<SupplyDrugsByDateDto> getSupplierByDateAndDrug(SupplyDrugsByDateDto dto) throws ParseException {
        //   String d="2019-11-19";
        // Date date1=new SimpleDateFormat("yyyy-MM-dd").parse(d);

        String date = new SimpleDateFormat("yyyy-MM-dd").format(dto.getExpirationDate());

//        Date exp = new SimpleDateFormat("yyyy-MM-dd").parse(expirationDate.toString());

        //   System.out.println(date);

        //    StringBuilder createSqlQuery = new StringBuilder("select  s.supplier_id,s.supplier_name ,d.expiration_date ,d.drug_card_id, count(*) from depot d inner join customer_supply_order cso on d.customer_supply_order_id=cso.customer_supply_order_id inner join supplier s on cso .supplier_id =s.supplier_id and d.expiration_date=to_timestamp('" +date+"', 'YYYY-MM-DD')\\:\\:timestamp without time zone group by s.supplier_id ,d.expiration_date ,d.drug_card_id ");


        System.out.println(date);
        StringBuilder createSqlQuery = new StringBuilder("select s.supplier_id,s.supplier_name ,count(*) from supplier s inner join customer_supply_order cso on cso.supplier_id=s.supplier_id  inner join depot d on cso.customer_supply_order_id=d.customer_supply_order_id and d.drug_card_id =" + dto.getDrugCard() + " and d.expiration_date = '" + date + "' and d.depot_status_id =10 group by s.supplier_name,s.supplier_id");
        List<Object> list2 = entityManager.createNativeQuery(createSqlQuery.toString()).getResultList();

        if (list2.size() == 0) {
            return null;
        }
        List<SupplyDrugsByDateDto> supplyDrugsByDateDtos = new ArrayList<>();

        for (Object o : list2) {
            StringBuilder sb = new StringBuilder(JSONObject.valueToString(o));
            System.out.println(sb);

            String supplierId = sb.substring(1, sb.indexOf(","));
            String supplierName = sb.substring(sb.indexOf(",") + 2, sb.lastIndexOf(",") - 1);
            String count = sb.substring(sb.lastIndexOf(",") + 1, sb.length() - 1);
            System.out.println(supplierId + supplierName + count);

//           System.out.println(listdata);
//           String replace = listdata.replace("[","");
//           System.out.println(replace);
//           String replace1 = replace.replace("]","");
//           System.out.println(replace1);
//           List<String> myList = Arrays.asList(replace1.split(","));
            SupplyDrugsByDateDto supplyDrugsByDateDto = new SupplyDrugsByDateDto();
            supplyDrugsByDateDto.setSupplier(Long.parseLong(supplierId));
            supplyDrugsByDateDto.setSupplierName(supplierName);
            supplyDrugsByDateDto.setCount(Long.parseLong(count));
            System.out.println(supplyDrugsByDateDto.toString());
            supplyDrugsByDateDtos.add(supplyDrugsByDateDto);

        }
//        for(int i=0;i<list2.size();i++){
//
//            StringBuilder sb=new StringBuilder(JSONObject.valueToString(list2.get(i)));
//            System.out.println(sb);
//
//
//
//        }

//        Map<Object,Long > map = list2.stream()
//                .collect(  Collectors.groupingBy(c ->c , Collectors.counting())) ;
//        map.forEach((k , v ) -> System.out.println(k + " : "+ v));

        System.out.println(supplyDrugsByDateDtos.toString());
        return supplyDrugsByDateDtos;
    }

    public List<SupplyDrugsByDateDto> getDrugsByDateAndDrugAndSupplier(SupplyDrugsByDateDto dto) throws ParseException {

        String date = new SimpleDateFormat("yyyy-MM-dd").format(dto.getExpirationDate());
        StringBuilder createSqlQuery = new StringBuilder("select cso .average_unit_price,count(*),d.customer_supply_order_id from customer_supply_order cso inner join depot d on cso.customer_supply_order_id =d.customer_supply_order_id and d.drug_card_id =" + dto.getDrugCard() + " and d.expiration_date = '" + date + "' and cso.supplier_id=" + dto.getSupplier() + "  and d.depot_status_id =10 group by cso.average_unit_price, d.customer_supply_order_id");
        List<Object> objectList = entityManager.createNativeQuery(createSqlQuery.toString()).getResultList();

        if (objectList.size() == 0) {
            return null;
        }
        List<SupplyDrugsByDateDto> supplyDrugsByDateDtos = new ArrayList<>();


        for (Object obj : objectList) {
            StringBuilder sb = new StringBuilder(JSONObject.valueToString(obj));
            System.out.println(sb);
            SupplyDrugsByDateDto supplyDrugsByDateDto = new SupplyDrugsByDateDto();
            String aup = sb.substring(1, sb.indexOf(","));
            supplyDrugsByDateDto.setAverageUnitPrice(Float.parseFloat(aup));
            String count = sb.substring(sb.indexOf(",") + 1, sb.lastIndexOf(","));
            String csoId = sb.substring(sb.lastIndexOf(",") + 1, sb.length() - 1);
            Optional<CustomerSupplyOrder> optCso=customerSupplyOrderRepository.findById(Long.parseLong(csoId));
            if(optCso.isPresent()){
              CustomerSupplyOrderDto cso=new CustomerSupplyOrderDto();
              cso.setCustomerSupplyOrderId(optCso.get().getCustomerSupplyOrderId());
              cso.setTotality(optCso.get().getTotality());
              cso.setOtherCompanyId(optCso.get().getOtherCompanyId());
              //******DÜZENLENECEK*****
//              if(optCso.get().getSupplyOrderPriceLiva()!=null){
//                  cso.setSupplyOrderPriceLiva(optCso.get().getSupplyOrderPriceLiva());
//              }
//              cso.setSupplyOrderPrice(optCso.get().getSupplyOrderPrice());
              supplyDrugsByDateDto.setCustomerSupplyOrder(cso);
            }
            supplyDrugsByDateDto.setCount(Long.parseLong(count));
            supplyDrugsByDateDtos.add(supplyDrugsByDateDto);

        }

        return supplyDrugsByDateDtos;
    }


    public Boolean getDrugsByRefundOffer(SupplyDrugsByDateDto dto) throws NotFoundException {

        String date = new SimpleDateFormat("yyyy-MM-dd").format(dto.getExpirationDate());
        StringBuilder createSqlQuery = new StringBuilder("select  d.depot_id from depot d inner join customer_supply_order cso on d.customer_supply_order_id =cso.customer_supply_order_id and cso.supplier_id =" + dto.getBoughtFrom() + " and d.expiration_date = '" + date + "'  and d.drug_card_id =" + dto.getDrugCard() + " and d.depot_status_id =10 ");
        //   StringBuilder createSqlQuery = new StringBuilder("select  d.depot_id from depot d inner join customer_supply_order cso on d.customer_supply_order_id =cso.customer_supply_order_id and cso.supplier_id =2  and cso.average_unit_price = '0.71814865' and d.expiration_date = '2031-10-21'  and d.drug_card_id =2 and d.depot_status_id =10");
        if (!supplierRepository.findById(dto.getSupplier()).isPresent()) {
            throw new NotFoundException("Böyle bir tedarikçi yok");
        }
        List<Object> objectList = entityManager.createNativeQuery(createSqlQuery.toString()).getResultList();

        int a = objectList.size();
        if (objectList.size() < dto.getCount()) {
            throw new NotFoundException("Stokta bu adar ilaç yok !!");
        }

        RefundOffer refundOffer = new RefundOffer();
        refundOffer.setCreatedAt(createdAt);
        refundOffer.setExpirationDate(dto.getExpirationDate());

        refundOffer.setUnitPrice(dto.getAverageUnitPrice());
        refundOffer.setTotality(dto.getCount());
        refundOffer.setOfferedTotality(dto.getCount());
        refundOffer.setRefundNote(dto.getRefundNote());
        refundOffer.setDrugCard(drugCardRepository.findById(dto.getDrugCard()).get());
        refundOffer.setSupplier(supplierRepository.findById(dto.getSupplier()).get());
        refundOffer.setRefundOfferStatus(refundOfferStatusRepository.findById(10L).get());
        refundOffer = refundOfferRepository.save(refundOffer);

        Double total = 0D;

        for (int i = 0; i < dto.getCount(); i++) {

            System.out.println(objectList.get(i));
            Optional<Depot> optionalDepot = depotRepository.findById(Long.parseLong(objectList.get(i).toString()));
            optionalDepot.get().setDepotStatus(depotStatusRepository.findById(5L).get());
            optionalDepot.get().setRefundOffer(refundOffer);
            depotRepository.save(optionalDepot.get());
            total += dto.getAverageUnitPrice();


        }
        refundOffer.setOtherCompanyId(dto.getOtherCompanyId());
        refundOffer.setTotalPrice(total);
        refundOffer.setOfferedTotalPrice(total);
        refundOfferRepository.save(refundOffer);

        return true;
    }

    public Boolean cancelRefundOffer(Long refundOfferId) throws NotFoundException {
        Optional<RefundOffer> optionalRefundOffer = refundOfferRepository.findById(refundOfferId);
        if (!optionalRefundOffer.isPresent()) {
            throw new NotFoundException("Böyle bir sipariş yok");
        }
        if (optionalRefundOffer.get().getRefundOfferStatus().getRefundOfferStatusId() != 10L) {
            throw new NotFoundException("Bu sipariş sizin tarafınızdan iptal olması için uygun değildir");
        }

        List<Depot> depotList = depotRepository.cancelRefundOffer(refundOfferId);
        for (Depot drugs : depotList) {
            drugs.setRefundOffer(null);
            drugs.setDepotStatus(depotStatusRepository.findById(10L).get());
            depotRepository.save(drugs);
        }
        optionalRefundOffer.get().setRefundOfferStatus(refundOfferStatusRepository.findById(30L).get());
        refundOfferRepository.save(optionalRefundOffer.get());
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

}
