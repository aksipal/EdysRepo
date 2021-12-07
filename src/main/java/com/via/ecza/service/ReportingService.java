package com.via.ecza.service;


import com.via.ecza.dto.*;
import com.via.ecza.entity.*;
import com.via.ecza.entity.enumClass.Role;
import com.via.ecza.repo.*;
import com.via.ecza.dto.AmountOfDrugsGroupByMonthsDto;
import com.via.ecza.dto.OrderQuantitiesByMonthDto;
import com.via.ecza.entity.User;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.*;

@Slf4j
@Service
@Transactional
public class ReportingService {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ControlService controlService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private CustomerOrderRepository customerOrderRepository;
    @Autowired
    private CustomerOrderDrugsRepository customerOrderDrugsRepository;
    @Autowired
    private ReportingRepository reportingRepository;
    @Autowired
    private ModelMapper mapper;

    public int getDrugsInStockCount(String authHeader) throws NotFoundException {
        StringBuilder createSqlQueryForStock = new StringBuilder(" select count(*) from depot d where d.depot_status_id =10 ");
        List<Object> stock = entityManager.createNativeQuery(createSqlQueryForStock.toString()).getResultList();
        int stockCount = Integer.valueOf(JSONObject.valueToString(stock.get(0)));

        StringBuilder createSqlQueryForDepot = new StringBuilder(" select count(*) from depot d where d.depot_status_id =1 ");
        List<Object> depot = entityManager.createNativeQuery(createSqlQueryForDepot.toString()).getResultList();
        int depotCount = Integer.valueOf(JSONObject.valueToString(depot.get(0)));


        return stockCount;
    }

    public int getDrugsInDepotCount(String authHeader) throws NotFoundException {
        StringBuilder createSqlQueryForDepot = new StringBuilder(" select count(*) from depot d where d.depot_status_id =1 ");
        List<Object> depot = entityManager.createNativeQuery(createSqlQueryForDepot.toString()).getResultList();
        int depotCount = Integer.valueOf(JSONObject.valueToString(depot.get(0)));

        return depotCount;
    }

    public int getCustomerOrderCount(String authHeader) throws NotFoundException {
        StringBuilder createSqlQueryForDepot = new StringBuilder(" select count(*) from customer_order co  ");
        List<Object> order = entityManager.createNativeQuery(createSqlQueryForDepot.toString()).getResultList();
        int orderCount = Integer.valueOf(JSONObject.valueToString(order.get(0)));

        return orderCount;
    }

    public int getPartiallyReceivedCustomerOrderCount(String authHeader) throws NotFoundException {
        StringBuilder createSqlQueryForDepot = new StringBuilder(" select count(*) from customer_order co where co.order_status_id =40  ");
        List<Object> partiallyOrder = entityManager.createNativeQuery(createSqlQueryForDepot.toString()).getResultList();
        int orderCount = Integer.valueOf(JSONObject.valueToString(partiallyOrder.get(0)));

        return orderCount;
    }

    public int getFullyReceivedCustomerOrderCount(String authHeader) throws NotFoundException {
        StringBuilder createSqlQueryForDepot = new StringBuilder(" select count(*) from customer_order co where co.order_status_id =50  ");
        List<Object> fullyOrder = entityManager.createNativeQuery(createSqlQueryForDepot.toString()).getResultList();
        int orderCount = Integer.valueOf(JSONObject.valueToString(fullyOrder.get(0)));

        return orderCount;
    }

    public int getRefundCounts(String authHeader) throws NotFoundException {
        StringBuilder createSqlQueryForDepot = new StringBuilder(" select count(*) from refund ro   ");
        List<Object> refundOffer = entityManager.createNativeQuery(createSqlQueryForDepot.toString()).getResultList();
        int orderCount = Integer.valueOf(JSONObject.valueToString(refundOffer.get(0)));

        return orderCount;
    }

    public int getCustumerSupplyOrderCounts(String authHeader) throws NotFoundException {
        StringBuilder createSqlQueryForDepot = new StringBuilder(" select count(*) from customer_supply_order cso   ");
        List<Object> supplyOrder = entityManager.createNativeQuery(createSqlQueryForDepot.toString()).getResultList();
        int orderCount = Integer.valueOf(JSONObject.valueToString(supplyOrder.get(0)));

        return orderCount;
    }

    public List<AmountOfDrugsGroupByMonthsDto> getAmountOfSendDrugsGroupByMonths(String authHeadcompler) throws NotFoundException {

        Date date=new Date();
        int year=date.getYear()+1900;


        StringBuilder createSqlQueryForDepot = new StringBuilder("select extract(month from d.sending_date) month_info, count (*) from depot d where d.depot_status_id =90 and extract(year from d.sending_date)="+year+
                " group by extract(month from d.sending_date) order by extract(month from d.sending_date)");
        List<Object[]> orderList = entityManager.createNativeQuery(createSqlQueryForDepot.toString()).getResultList();

        List<AmountOfDrugsGroupByMonthsDto> sendList = createSendList();




        for (Object[] objArr : orderList) {
            AmountOfDrugsGroupByMonthsDto dto = new AmountOfDrugsGroupByMonthsDto();
            dto.setDrugCount(Long.valueOf(String.valueOf((BigInteger) objArr[1])));
            Double value = ((Double) objArr[0]);
            dto.setMonthValue((int) value.longValue());
            sendList.set(dto.getMonthValue()-1,dto);

        }


        return sendList;
    }

    public List<AmountOfDrugsGroupByMonthsDto> createSendList(){
        List<AmountOfDrugsGroupByMonthsDto> sendList = new ArrayList<>();

        for(int i=0;i<12;i++){
            AmountOfDrugsGroupByMonthsDto sampleItem=new AmountOfDrugsGroupByMonthsDto();
            sampleItem.setMonthValue(i+1);
            sampleItem.setDrugCount(0L);
            sendList.add(sampleItem);
        }

        return sendList;
    }





    public List<OrderQuantitiesByMonthDto> getOrderQuantitiesByMonth(String authHeadcompler) throws NotFoundException {

        Date date=new Date();
        int year=date.getYear()+1900;


        StringBuilder createSqlQueryForDepot = new StringBuilder("select extract(month from co.order_date) month_info, count(*) from customer_order co where co.order_status_id =65 and extract (year from co.order_date)="+year+
                "group by extract(month from co.order_date) order by extract(month from co.order_date)");
        List<Object[]> orderList = entityManager.createNativeQuery(createSqlQueryForDepot.toString()).getResultList();



        List<OrderQuantitiesByMonthDto> sendList = createsSendList();


        for (Object[] objArr : orderList) {
            OrderQuantitiesByMonthDto dto = new OrderQuantitiesByMonthDto();
            dto.setDrugCount(Long.valueOf(String.valueOf((BigInteger) objArr[1])));
            Double value = ((Double) objArr[0]);
            dto.setMonthValue((int) value.longValue());
            sendList.set(dto.getMonthValue()-1,dto);
        }
        return sendList;
    }

    public List<OrderQuantitiesByMonthDto> createsSendList(){
        List<OrderQuantitiesByMonthDto> sendList = new ArrayList<>();
        for(int i=0;i<12;i++){
            OrderQuantitiesByMonthDto sampleItem=new OrderQuantitiesByMonthDto();
            sampleItem.setMonthValue(i+1);
            sampleItem.setDrugCount(0L);
            sendList.add(sampleItem);
        }
        return sendList;
    }

    public int getShowOrderPendingConfirmationCount(String authHeader) throws NotFoundException {
        StringBuilder createSqlQueryForDepot = new StringBuilder("select count(*) from customer_order co where co.order_status_id =10");
        List<Object> supplyOrder = entityManager.createNativeQuery(createSqlQueryForDepot.toString()).getResultList();
        int orderCount = Integer.valueOf(JSONObject.valueToString(supplyOrder.get(0)));
        return orderCount;
    }

    public int getCustomerOrderPricingPhase(String authHeader) throws NotFoundException {
        StringBuilder createSqlQueryForDepot = new StringBuilder("select count(*) from customer_order  co where co.order_status_id =2");
        List<Object> supplyOrder = entityManager.createNativeQuery(createSqlQueryForDepot.toString()).getResultList();
        int orderCount = Integer.valueOf(JSONObject.valueToString(supplyOrder.get(0)));
        return orderCount;
    }

    public int getReceiptAdminApprove(String authHeader) throws NotFoundException {
        StringBuilder createSqlQueryForDepot = new StringBuilder("select count(*) from receipt r where r.receipt_status_id=10");
        List<Object> supplyOrder = entityManager.createNativeQuery(createSqlQueryForDepot.toString()).getResultList();
        int orderCount = Integer.valueOf(JSONObject.valueToString(supplyOrder.get(0)));
        return orderCount;
    }

    public int getReceiptManagerApprove(String authHeader) throws NotFoundException {
        StringBuilder createSqlQueryForDepot = new StringBuilder("select count(*) from receipt r where r.receipt_status_id=20");
        List<Object> supplyOrder = entityManager.createNativeQuery(createSqlQueryForDepot.toString()).getResultList();
        int orderCount = Integer.valueOf(JSONObject.valueToString(supplyOrder.get(0)));
        return orderCount;
    }
    //İhracatçı sayfası fiyatlandırma bekleyen card
    public int getCustomerOrderConfirmation(String authHeader) throws NotFoundException {
        User user = controlService.getUserFromToken(authHeader);
        StringBuilder createSqlQueryForDepot = new StringBuilder("select count(*) from customer_order co where co.order_status_id =1 and user_id=" + user.getUserId());
        List<Object> supplyOrder = entityManager.createNativeQuery(createSqlQueryForDepot.toString()).getResultList();
        int orderCount = Integer.valueOf(JSONObject.valueToString(supplyOrder.get(0)));
        return orderCount;
    }
    //İhracatçı sayfası satın alma onayı card
    public int getCustomerOrderPurchaseConfirmation(String authHeader) throws NotFoundException {
        User user = controlService.getUserFromToken(authHeader);
        StringBuilder createSqlQueryForDepot = new StringBuilder("select count(*) from customer_order co where co.order_status_id =7 and user_id=" + user.getUserId());
        List<Object> supplyOrder = entityManager.createNativeQuery(createSqlQueryForDepot.toString()).getResultList();
        int orderCount = Integer.valueOf(JSONObject.valueToString(supplyOrder.get(0)));
        return orderCount;
    }
    public int getSupplierOrderSearchConfirmation(String authHeader) throws NotFoundException {
        StringBuilder createSqlQueryForDepot = new StringBuilder("select count(*) from customer_order co where co.order_status_id =15");
        List<Object> supplyOrder = entityManager.createNativeQuery(createSqlQueryForDepot.toString()).getResultList();
        int orderCount = Integer.valueOf(JSONObject.valueToString(supplyOrder.get(0)));
        return orderCount;
    }
    public int getAcceptanceDrugConfirmation(String authHeader) throws NotFoundException {
        StringBuilder createSqlQueryForDepot = new StringBuilder("select count(*) from customer_supply_order cso where cso.customer_supply_status_id =10");
        List<Object> supplyOrder = entityManager.createNativeQuery(createSqlQueryForDepot.toString()).getResultList();
        int orderCount = Integer.valueOf(JSONObject.valueToString(supplyOrder.get(0)));
        return orderCount;
    }

    //İhracatçı sayfası yurt içi satın alma card
    public int getCustomerOrderSendToBuy(String authHeader) throws NotFoundException {
        User user = controlService.getUserFromToken(authHeader);
        StringBuilder createSqlQueryForDepot = new StringBuilder("select count(*) from customer_order co where co.order_status_id =14 and user_id=" + user.getUserId());
        List<Object> supplyOrder = entityManager.createNativeQuery(createSqlQueryForDepot.toString()).getResultList();
        int orderCount = Integer.valueOf(JSONObject.valueToString(supplyOrder.get(0)));
        return orderCount;
    }
    public int getUserOrderCount(String authHeader) throws NotFoundException {
        User user = controlService.getUserFromToken(authHeader);
        StringBuilder createSqlQueryForDepot = new StringBuilder("select count(*) from customer_order co where co.user_id=" + user.getUserId());
        List<Object> supplyOrder = entityManager.createNativeQuery(createSqlQueryForDepot.toString()).getResultList();
        int orderCount = Integer.valueOf(JSONObject.valueToString(supplyOrder.get(0)));
        return orderCount;
    }

    public int getUserPartiallyReceivedOrders(String authHeader) throws NotFoundException {
        User user = controlService.getUserFromToken(authHeader);
        StringBuilder createSqlQueryForDepot = new StringBuilder("select count(*) from customer_order co where co.order_status_id =40 and user_id=" + user.getUserId());
        List<Object> supplyOrder = entityManager.createNativeQuery(createSqlQueryForDepot.toString()).getResultList();
        int orderCount = Integer.valueOf(JSONObject.valueToString(supplyOrder.get(0)));
        return orderCount;
    }
    public int getUserFullyReceivedCustomerOrderCount(String authHeader) throws NotFoundException {
        User user = controlService.getUserFromToken(authHeader);
        StringBuilder createSqlQueryForDepot = new StringBuilder("select count(*) from customer_order co where co.order_status_id =50 and user_id=" + user.getUserId());
        List<Object> supplyOrder = entityManager.createNativeQuery(createSqlQueryForDepot.toString()).getResultList();
        int orderCount = Integer.valueOf(JSONObject.valueToString(supplyOrder.get(0)));
        return orderCount;
    }
    public int getPartiallyReceivedOrders(String authHeader) throws NotFoundException {
        User user = controlService.getUserFromToken(authHeader);
        StringBuilder createSqlQueryForDepot = new StringBuilder("select count(*) from customer_supply_order cso where cso.customer_supply_status_id =45");
        List<Object> supplyOrder = entityManager.createNativeQuery(createSqlQueryForDepot.toString()).getResultList();
        int orderCount = Integer.valueOf(JSONObject.valueToString(supplyOrder.get(0)));
        return orderCount;
    }

    public int getShowSupplierChecklist(String authHeader) throws NotFoundException {
        User user = controlService.getUserFromToken(authHeader);
        StringBuilder createSqlQueryForDepot = new StringBuilder("select count(*) from supplier_offer so where so.supplier_offer_status_id =10");
        List<Object> supplyOrder = entityManager.createNativeQuery(createSqlQueryForDepot.toString()).getResultList();
        int orderCount = Integer.valueOf(JSONObject.valueToString(supplyOrder.get(0)));
        return orderCount;
    }



    //ihracatci bazlı siparislerin getirilmesi
    public List<CustomerOrderForExporterDto> getSingleExporterOrder(String authHeader,Long userId) throws NotFoundException {
       // User user = controlService.getUserFromToken(authHeader);

        Date date=new Date();
        int year=date.getYear()+1900;



        StringBuilder createSqlQuery = new StringBuilder("select co.customer_order_no customerOrderNo from customer_order co inner join depot d on d.customer_order_id =co.customer_order_id where d.depot_status_id =90 and co.user_id =" + userId + " group by co.customer_order_id ");

        List<Object> orderList = entityManager.createNativeQuery(createSqlQuery.toString()).getResultList();

         List<CustomerOrderForExporterDto> orderLists = new ArrayList<>();



        for (Object objArr : orderList) {

            //  kullanici bilgisi tek value set etmek
//            CustomerOrderForExporterDto dto = new CustomerOrderForExporterDto();
//            dto.setCustomerOrderNo(String.valueOf(objArr));

            //Kullanici Bilgileri mapper ile dönüştürülüyor....371 xD
            CustomerOrder customerOrder=customerOrderRepository.findByCustomerOrderNo(String.valueOf(objArr)).get();
            CustomerOrderForExporterDto dto=mapper.map(customerOrder,CustomerOrderForExporterDto.class);
            orderLists.add(dto);
        }

        return orderLists;
    }





//
//            List<CustomerOrder> customerOrders = createSqlQuery
//            CustomerOrderForExporterDto[] array = mapper.map(customerOrders, CustomerOrderForExporterDto[].class);
//            List<CustomerOrderForExporterDto> dtos = Arrays.asList(array);
//            List<CustomerOrderForExporterDto> csoList = entityManager.createNativeQuery(createSqlQuery.toString(),CustomerOrder.class).getResultList();

//            for (Object[] objArr : listResult) {
//                SingleExporterOrderDto dto = new SingleExporterOrderDto();
//                dto.setCustomerOrderNo(String.valueOf(objArr[5]));
//                Timestamp timestamp=(Timestamp) objArr[2];
//                Date date = new Date(timestamp.getTime());
//                dto.setOrderDate(date);
//
//                Company company= companyRepository.findById(Long.valueOf(String.valueOf((BigInteger) objArr[17]))).get();
//                SingleCompanyDto singleCompanyDto=mapper.map(company,SingleCompanyDto.class);
//                dto.setCompany(singleCompanyDto);
//
//                Customer customer = customerRepository.findById(Long.valueOf(String.valueOf((BigInteger) objArr[18]))).get();
//                SingleCustomerDto singleCustomerDto = mapper.map(customer,SingleCustomerDto.class);
//                dto.setCustomer(singleCustomerDto);
//
//                dtosList.add(dto);
//            }


    //ihracatci bazlı siparislerin altındaki ilacların getirilmesi
    public List<SingleCustomerOrderDto> getOrderByCustomerOrderId(Long customerOrderId) {
        StringBuilder createSqlQuery = new StringBuilder("select * from customer_order co where co.customer_order_id = " + customerOrderId);
        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), CustomerOrder.class).getResultList();
        SingleCustomerOrderDto[] dtos = mapper.map(list,SingleCustomerOrderDto[].class );
        List<SingleCustomerOrderDto> dtosList  = Arrays.asList(dtos);
        return dtosList;
    }


    public List<TotalOrderQuantityByExportersDto> getTotalOrderQuantityByExporters(String authHeadcompler) throws NotFoundException {

        List<TotalOrderQuantityByExportersDto> userList = new ArrayList<>();

        Date date=new Date();
        int year=date.getYear()+1900;

        StringBuilder createSqlQueryForDepot = new StringBuilder( " select a.*,count(a.*)  from (select co.user_id  from depot d" +
                " inner join customer_order co on co.customer_order_id=d.customer_order_id where d.depot_status_id =90 and extract(year from co.created_date)= "+year+
                "group by co.user_id,co.customer_order_id order by count(co.customer_order_id) desc) a group by a.user_id" );
        List<Object[]> orderList = entityManager.createNativeQuery(createSqlQueryForDepot.toString()).getResultList();

        for (Object[] objArr : orderList) {
            TotalOrderQuantityByExportersDto dto = new TotalOrderQuantityByExportersDto();
            Long userCount = Long.valueOf(String.valueOf((BigInteger) objArr[0]));
            Optional<User> optUser = userRepository.findById(userCount);
            dto.setUser(optUser.get());
            dto.setOrderCount(Long.valueOf(String.valueOf((BigInteger) objArr[1])));
            userList.add(dto);

        }

        return userList;
    }




    public int getCreateAnInvoice(String authHeader) throws NotFoundException {
        User user = controlService.getUserFromToken(authHeader);
        StringBuilder createSqlQueryForDepot = new StringBuilder("select count(*)  from receipt r where r.receipt_status_id =30");
        List<Object> supplyOrder = entityManager.createNativeQuery(createSqlQueryForDepot.toString()).getResultList();
        int orderCount = Integer.valueOf(JSONObject.valueToString(supplyOrder.get(0)));
        return orderCount;
    }

    //line chart kodu-devam edilecek
//    public CompletedOrdersDto getCompletedSupplyOrdersCount(String authHeader) throws NotFoundException {
//        Date date = new Date();
//        StringBuilder createSqlQueryForSupply = new StringBuilder("select count(*),extract(month from cso.created_at) from customer_supply_order cso where cso.customer_supply_status_id =50 and extract(year from cso.created_at)=2021 group by extract(month from cso.created_at) ");
//        List<CompletedOrdersDto> dtosList = new ArrayList<CompletedOrdersDto>();
//        List<Object[]> order1 = entityManager.createNativeQuery(createSqlQueryForSupply.toString()).getResultList();
////        long supplyOrderCount = Long.valueOf(JSONObject.valueToString(order1.get(0)));
//
//        for (Object[] objArr : order1) {
////            CompletedOrdersDto completedOrdersDto = new CompletedOrdersDto();
////            completedOrdersDto.setSupplyOrderCount(supplyOrderCount);
////
////            dtosList.add(completedOrdersDto);
//            CompletedOrdersDto completedOrdersDto = new CompletedOrdersDto();
//            completedOrdersDto.setSupplyOrderCount(Long.valueOf(String.valueOf((BigInteger)objArr[0])));
//            completedOrdersDto.setMonthValue(Long.valueOf(String.valueOf((Double) objArr[1])));
//            dtosList.add(completedOrdersDto);
//
////            System.out.println((BigInteger)objArr[0]);
////            System.out.println((Double) objArr[1]);
//        }
//
////        StringBuilder createSqlQueryForCustomer = new StringBuilder(" select count(*),extract(month from co.created_date) from customer_order co where co.order_status_id =50 and extract(year from co.created_date)=2021 group by extract(month from co.created_date) ");
////        List<Object> order2 = entityManager.createNativeQuery(createSqlQueryForCustomer.toString()).getResultList();
////        long customerOrderCount = Long.valueOf(JSONObject.valueToString(order2.get(0)));
////
////        CompletedOrdersDto completedOrdersDto = new CompletedOrdersDto();
////        completedOrdersDto.setSupplyOrderCount(supplyOrderCount);
////        completedOrdersDto.setCustomerOrderCount(customerOrderCount);
//
//        return null;
//    }
//    public CompletedOrdersDto getCompletedCustomerOrdersCount(String authHeader) throws NotFoundException {
//        StringBuilder createSqlQueryForDepot = new StringBuilder(" select count(*) from customer_order co where co.order_status_id =50 ");
//        List<Object> order = entityManager.createNativeQuery(createSqlQueryForDepot.toString()).getResultList();
//        long customerOrderCount = Long.valueOf(JSONObject.valueToString(order.get(0)));
//
//        CompletedOrdersDto completedOrdersDto = new CompletedOrdersDto();
//        completedOrdersDto.setCustomerOrderCount(customerOrderCount);
//
//        return completedOrdersDto;
//    }

}
