package com.via.ecza.manager;


import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.via.ecza.dto.*;
import com.via.ecza.entity.*;
import com.via.ecza.entity.enumClass.Role;
import com.via.ecza.manager.service.CustomerOrderService;
import com.via.ecza.repo.*;
import com.via.ecza.service.ControlService;
import com.via.ecza.service.CustomerOrderStatusHistoryService;
import javassist.NotFoundException;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.io.*;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

@Service
@Transactional
public class CustomerOrderManager  implements CustomerOrderService {

    @Autowired
    private CustomerOrderRepository customerOrderRepository;
    @Autowired
    private CustomerOrderDrugsRepository customerOrderDrugsRepository;
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
    private CountryRepository countryRepository;
    @Autowired
    private CustomerOrderStatusRepository orderStatusRepository;
    @Autowired
    private PurchaseStatusRepository purchaseStatusRepository;
    @Autowired
    private PurchaseOrderDrugsRepository purchaseOrderDrugsRepository;
    @Autowired
    private BoxRepository boxRepository;
    @Autowired
    private SmallBoxRepository smallBoxRepository;
    @Autowired
    private CustomerOrderStatusHistoryService customerOrderStatusHistoryService;
    @Autowired
    private DepotRepository depotRepository;
    @Autowired
    private DepotStatusRepository depotStatusRepository;


    public SingleCustomerOrderDto getSingleCustomerOrder(String authHeader, Long customerId, Long customerOrderId) throws Exception {
        try {
            SingleCustomerOrderDto dto = null;
            Optional<CustomerOrder> optionalCustomerOrder = null;
            Optional<Customer> optionalCustomer= null;
            User user = this.getUserFromToken(authHeader);
            if (user.getRole() == Role.ADMIN || user.getRole() == Role.MANAGER) {
                optionalCustomer = customerRepository.findById(customerId);
                if (!optionalCustomer.isPresent())          throw new Exception("Müşteri kaydı bulunamadı");

                optionalCustomerOrder = customerOrderRepository.getSingleCustomerOrder(customerOrderId, optionalCustomer.get().getCustomerId());
                if (!optionalCustomerOrder.isPresent())     throw new Exception("Sipariş kaydı bulunamadı");

            } else if (user.getRole() == Role.EXPORTER) {
                optionalCustomer = customerRepository.findById(customerId);
                if (!optionalCustomer.isPresent())          throw new Exception("Müşteri kaydı bulunamadı");

                optionalCustomerOrder = customerOrderRepository.getSingleCustomerOrderForExporter(
                        customerOrderId,
                        optionalCustomer.get().getCustomerId(),
                        user.getUserId());
                if (!optionalCustomerOrder.isPresent())     throw new Exception("Sipariş kaydı bulunamadı");

            }
            CustomerOrder customerOrder = optionalCustomerOrder.get();
            dto = mapper.map(customerOrder, SingleCustomerOrderDto.class);
            dto.setCustomerOrderDrugs(null);
            return dto;
        } catch (Exception e) {
            throw e;
        }
    }
    public SingleCustomerOrderDto getSingleCustomerOrderForManager(String authHeader, Long customerId, Long customerOrderId) throws Exception {

            SingleCustomerOrderDto dto = new SingleCustomerOrderDto();
            User user = this.getUserFromToken(authHeader);
            if (user.getRole() == Role.ADMIN || user.getRole() == Role.MANAGER) {
                Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
                if (!optionalCustomer.isPresent()) {
                    throw new Exception("Müşteri kaydı bulunamadı");
                }

                Optional<CustomerOrder> optionalCustomerOrder = customerOrderRepository.getSingleCustomerOrder(customerOrderId, optionalCustomer.get().getCustomerId());
                if (!optionalCustomerOrder.isPresent()) {
                    throw new Exception("Sipariş kaydı bulunamadı");
                }
                dto = mapper.map(optionalCustomerOrder.get(), SingleCustomerOrderDto.class);
                Collections.sort(dto.getCustomerOrderDrugs(), new Comparator<CustomerOrderDrugsListDto>() {
                    @Override
                    public int compare(CustomerOrderDrugsListDto d1, CustomerOrderDrugsListDto d2) {
                        return d1.getCustomerOrderDrugId().compareTo(d2.getCustomerOrderDrugId());
                    }
                });
                return dto;
            } else
                return null;
    }
    public Boolean sendtoSetOrderPrices(String authHeader, Long customerId, Long customerOrderId) throws Exception {
        try {
            User user = this.getUserFromToken(authHeader);
            Optional<CustomerOrderStatus> optionalCustomerOrderStatus = orderStatusRepository.findById(2L);
            Optional<CustomerOrder> optionalCustomerOrder = null;

            if (user.getRole() == Role.ADMIN) {
                Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
                if (!optionalCustomer.isPresent()) {
                    throw new Exception("Müşteri kaydı bulunamadı");
                }
                optionalCustomerOrder = customerOrderRepository.getSingleCustomerOrder(customerOrderId, optionalCustomer.get().getCustomerId());
                if (!optionalCustomerOrder.isPresent()) {
                    throw new Exception("Sipariş kaydı bulunamadı");
                }
            } else if (user.getRole() == Role.EXPORTER) {
                Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
                if (!optionalCustomer.isPresent()) {
                    throw new Exception("Müşteri kaydı bulunamadı");
                }
                optionalCustomerOrder = customerOrderRepository.getSingleCustomerOrderForExporter(
                        customerOrderId,
                        optionalCustomer.get().getCustomerId(),
                        user.getUserId());

                if (!optionalCustomerOrder.isPresent()) {
                    throw new Exception("Sipariş kaydı bulunamadı");
                }
            }
            CustomerOrder customerOrder = optionalCustomerOrder.get();
            if(customerOrder.getCustomerOrderDrugs() == null)
                return false;
            if(customerOrder.getCustomerOrderDrugs().size() <0)
                return false;
            customerOrder.setLogisticStatus(0);
            if(customerOrder.getOrderStatus().getOrderStatusId()>15)
                customerOrder.setOrderStatusHistory(customerOrder.getOrderStatus().getOrderStatusId());
            customerOrder.setOrderStatus(optionalCustomerOrderStatus.get());
            if(!customerOrderStatusHistoryService.save(customerOrder,optionalCustomerOrderStatus.get()))
                throw new Exception("Sipariş Oluşturulamadı");
            customerOrder = customerOrderRepository.save(customerOrder);
            return true;
        } catch (Exception e) {
            throw e;
        }
    }

    public Long save(String authHeader, @Valid CustomerOrderSaveDto dto) throws Exception {

            CustomerOrder customerOrder = mapper.map(dto, CustomerOrder.class);
            String username = controlService.getUsernameFromToken(authHeader);
            Optional<User> optUser = userRepository.findByUsername(username);
            if (!optUser.isPresent()) {
                throw new NotFoundException("Kullanıcı Bulunamadı");
            }

            Optional<CustomerOrderStatus> optOrderStatus = orderStatusRepository.findById(1L);
            if (!optOrderStatus.isPresent()) {
                throw new NotFoundException("Not found Customer Order Status");
            }

            if(dto.getCustomerId() == null){
                throw new NotFoundException("Müşteri Seçiniz.");
            }
            Optional<Customer> optCustomer = customerRepository.findById(dto.getCustomerId());
            if (!optCustomer.isPresent()) {
                throw new NotFoundException("Müşteri Bulunamadı");
            }
            customerOrder.setStatus(1);
            customerOrder.setCurrencyFee(Double.valueOf(1));
            customerOrder.setOrderDate(new Date());
            customerOrder.setCreatedDate(new Date());
            customerOrder.setUser(optUser.get());
            customerOrder.setOrderStatus(optOrderStatus.get());
            if(!customerOrderStatusHistoryService.save(customerOrder,optOrderStatus.get()))
                throw new Exception("Sipariş Oluşturulamadı");
            customerOrder.setCustomer(optCustomer.get());
            customerOrder.setPurchaseOrderNote("");
            customerOrder.setLogisticStatus(0);
            customerOrder.setCurrencyType(dto.getCurrencyType());
            if (optCustomer.get().getCompany() != null)
                customerOrder.setCompany(optCustomer.get().getCompany());
            optCustomer.get().getCustomerOrders().add(customerOrder);
            customerOrder = customerOrderRepository.save(customerOrder);

            // determine customer order no by using customer order id . such as : SIP-202000001 or SIP-202000002
            customerOrder.setCustomerOrderNo(getCode(customerOrder.getCustomerOrderId()));
            customerOrder = customerOrderRepository.save(customerOrder);
            return customerOrder.getCustomerOrderId();

    }

    public String getCode(Long customerOrdeId) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH)+1;
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        String code = null;
        if(String.valueOf(month).length() == 1)
            code = "SIP-" + year + "0" + month + day;
        else
            code = "SIP-" + year + "0" + month + day;
        int size = customerOrdeId.toString().length();
        for (int i = 0; i < 7 - size; i++)
            code += "0";
        code += customerOrdeId;
        return code;
    }

    public Page<CustomerOrderSearchListDto> getAllWithPagee(String authHeader, Pageable page, SearchCustomerOrderDto dto) throws Exception {
        try {
            StringBuilder createSqlQuery = null;
            List<CustomerOrder> list = null;
            Page<CustomerOrderSearchListDto> pageList = null;
            User user = this.getUserFromToken(authHeader);
            if (user.getRole() == Role.ADMIN) {
                createSqlQuery = new StringBuilder("select * from customer_order co ");
                if (dto.getCustomerId() != null || dto.getCity() != null || dto.getCountryId() != null) {

                    createSqlQuery.append("join customer c on c.customer_id = co.customer_id  where co.status=1 ");

                    if(dto.getOrderStatusId() == null)
                        createSqlQuery.append("and co.order_status_id != 65 ");

                    if (dto.getCustomerId() != null) if (dto.getCustomerId() != 0)
                        createSqlQuery.append("and c.customer_id = " + dto.getCustomerId() + " ");

                    if (dto.getCountryId() != null) if (dto.getCountryId() != 0)
                        createSqlQuery.append("and c.countryid = " + dto.getCountryId() + " ");

                    if (dto.getCity() != null)
                        createSqlQuery.append("and  c.city ILIKE '%" + dto.getCity() + "%' ");
                } else {
                    if(dto.getOrderStatusId() == null)
                        createSqlQuery.append("where co.status=1 and co.order_status_id != 65 ");
                    else
                        createSqlQuery.append(" where co.status=1 ");
                }


                if(dto.getUserId() != null)
                    createSqlQuery.append("and  co.user_id = " + dto.getUserId() + " ");

                if (dto.getCustomerOrderNo() != null)
                    createSqlQuery.append("and  co.customer_order_no ILIKE '%" + dto.getCustomerOrderNo() + "%' ");

                if (dto.getOrderStatusId() != null)
                    createSqlQuery.append("and  co.order_status_id = " + dto.getOrderStatusId() + " ");

                if (dto.getOrderDate() != null)
                    createSqlQuery.append("and co.order_date = " + dto.getOrderDate() + " ");
                if(page.getPageNumber()==0){
                    createSqlQuery.append("order by co.customer_order_id desc, co.customer_order_id limit "+page.getPageSize()+" offset "+page.getPageNumber());
                }else{
                    createSqlQuery.append("order by co.customer_order_id desc, co.customer_order_id limit "+page.getPageSize()+" offset "+(page.getPageSize()*page.getPageNumber()));
                }



            } else if (user.getRole() == Role.EXPORTER) {
                createSqlQuery = new StringBuilder("select * from customer_order co ");
                if (dto.getCustomerId() != null || dto.getCity() != null || dto.getCountryId() != null) {

                    createSqlQuery.append("join customer c on c.customer_id = co.customer_id  where co.status=1 and user_id=" + user.getUserId());

                    if(dto.getOrderStatusId() == null)
                        createSqlQuery.append("and co.order_status_id != 65 ");

                    if (dto.getCustomerId() != 0)
                        createSqlQuery.append("and c.customer_id = " + dto.getCustomerId() + " ");

                    if (dto.getCountryId() != 0)
                        createSqlQuery.append("and c.countryid = " + dto.getCountryId() + " ");

                    if (dto.getCity() != null)
                        createSqlQuery.append("and  c.city ILIKE '%" + dto.getCity() + "%' ");
                } else {
                    if(dto.getOrderStatusId() == null)
                        createSqlQuery.append(" where co.status=1 and user_id=" + user.getUserId() +" and co.order_status_id != 65");
                    else
                        createSqlQuery.append(" where co.status=1 and user_id=" + user.getUserId());
                }
                if (dto.getCustomerOrderNo() != null)
                    createSqlQuery.append("and  co.customer_order_no ILIKE '%" + dto.getCustomerOrderNo() + "%' ");

                if (dto.getOrderStatusId() != null)
                    createSqlQuery.append("and  co.order_status_id = " + dto.getOrderStatusId() + " ");

                if (dto.getOrderDate() != null) {
                    createSqlQuery.append("and co.order_date = " + dto.getOrderDate() + " ");
                }

                if(page.getPageNumber()==0){
                    createSqlQuery.append("order by co.customer_order_id desc, co.customer_order_id limit "+page.getPageSize()+" offset "+page.getPageNumber());
                }else{
                    createSqlQuery.append("order by co.customer_order_id desc, co.customer_order_id limit "+page.getPageSize()+" offset "+(page.getPageSize()*page.getPageNumber()));
                }
            }

            list = entityManager.createNativeQuery(createSqlQuery.toString(), CustomerOrder.class).getResultList();

            CustomerOrderSearchListDto[] dtos = mapper.map(list, CustomerOrderSearchListDto[].class);
            List<CustomerOrderSearchListDto> dtosList = Arrays.asList(dtos);

//            int start = Math.min((int) page.getOffset(), dtosList.size());
//            int end = Math.min((start + page.getPageSize()), dtosList.size());

            int start=0;
            int end=dtosList.size();
            int totalCount=0;

            if (user.getRole() == Role.ADMIN) {
                createSqlQuery = new StringBuilder("select count(*) from customer_order co ");
                if (dto.getCustomerId() != null || dto.getCity() != null || dto.getCountryId() != null) {

                    createSqlQuery.append("join customer c on c.customer_id = co.customer_id  where co.status=1 ");

                    if(dto.getOrderStatusId() == null)
                        createSqlQuery.append("and co.order_status_id != 65 ");

                    if (dto.getCustomerId() != null) if (dto.getCustomerId() != 0)
                        createSqlQuery.append("and c.customer_id = " + dto.getCustomerId() + " ");

                    if (dto.getCountryId() != null) if (dto.getCountryId() != 0)
                        createSqlQuery.append("and c.countryid = " + dto.getCountryId() + " ");

                    if (dto.getCity() != null)
                        createSqlQuery.append("and  c.city ILIKE '%" + dto.getCity() + "%' ");
                } else {
                    if(dto.getOrderStatusId() == null)
                        createSqlQuery.append("where co.status=1 and co.order_status_id != 65 ");
                    else
                        createSqlQuery.append(" where co.status=1 ");
                }

                if(dto.getUserId() != null)
                    createSqlQuery.append("and  co.user_id = " + dto.getUserId() + " ");

                if (dto.getCustomerOrderNo() != null)
                    createSqlQuery.append("and  co.customer_order_no ILIKE '%" + dto.getCustomerOrderNo() + "%' ");

                if (dto.getOrderStatusId() != null)
                    createSqlQuery.append("and  co.order_status_id = " + dto.getOrderStatusId() + " ");

                if (dto.getOrderDate() != null)
                    createSqlQuery.append("and co.order_date = " + dto.getOrderDate() + " ");
            } else if (user.getRole() == Role.EXPORTER) {
                createSqlQuery = new StringBuilder("select count(*) from customer_order co ");
                if (dto.getCustomerId() != null || dto.getCity() != null || dto.getCountryId() != null) {

                    createSqlQuery.append("join customer c on c.customer_id = co.customer_id  where co.status=1 and user_id=" + user.getUserId());

                    if(dto.getOrderStatusId() == null)
                        createSqlQuery.append("and co.order_status_id != 65 ");

                    if (dto.getCustomerId() != 0)
                        createSqlQuery.append("and c.customer_id = " + dto.getCustomerId() + " ");

                    if (dto.getCountryId() != 0)
                        createSqlQuery.append("and c.countryid = " + dto.getCountryId() + " ");

                    if (dto.getCity() != null)
                        createSqlQuery.append("and  c.city ILIKE '%" + dto.getCity() + "%' ");
                } else {
                    if(dto.getOrderStatusId() == null)
                        createSqlQuery.append(" where co.status=1 and user_id=" + user.getUserId() +" and co.order_status_id != 65");
                    else
                        createSqlQuery.append(" where co.status=1 and user_id=" + user.getUserId());
                }
                if (dto.getCustomerOrderNo() != null)
                    createSqlQuery.append("and  co.customer_order_no ILIKE '%" + dto.getCustomerOrderNo() + "%' ");

                if (dto.getOrderStatusId() != null)
                    createSqlQuery.append("and  co.order_status_id = " + dto.getOrderStatusId() + " ");

                if (dto.getOrderDate() != null) {
                    createSqlQuery.append("and co.order_date = " + dto.getOrderDate() + " ");
                }
            }

            List<Object> countList = entityManager.createNativeQuery(createSqlQuery.toString()).getResultList();
            for(Object data :countList){
                totalCount= Integer.valueOf(String.valueOf((BigInteger) data));
            }

            pageList = new PageImpl<>(dtosList.subList(start, end), page, totalCount);

            return pageList;

        } catch (Exception e) {
            throw e;
        }

    }

//    public List<CustomerOrderListDto> search(CustomerOrderSearchDto dto) {
//
//        List<CustomerOrderListDto> dtoList = null;
//        StringBuilder createSqlQuery = new StringBuilder("select * from customer_order co ");
//
//        if (dto.getCustomerId() != 0 || dto.getCity() != null || dto.getCountryId() != 0) {
//
//            createSqlQuery.append("join customer c on c.customer_id = co.customer_id  where co.status=1 ");
//            if (dto.getCustomerId() != 0) {
//                createSqlQuery.append("and c.customer_id = " + dto.getCustomerId() + " ");
//            }
//            if (dto.getCountryId() != 0) {
//                createSqlQuery.append("and c.countryid = " + dto.getCountryId() + " ");
//            }
//            if (dto.getCity() != null) {
//                createSqlQuery.append("and  c.city ILIKE '%" + dto.getCity() + "%' ");
//            }
//        } else {
//            createSqlQuery.append(" where co.status=1 ");
//        }
//
//        if (dto.getCustomerOrderNo() != null)
//            createSqlQuery.append("and  co.customer_order_no ILIKE '%" + dto.getCustomerOrderNo() + "%' ");
//
//        if (dto.getOrderStatusId() != 0)
//            createSqlQuery.append("and  co.order_status_id = " + dto.getOrderStatusId() + " ");
//        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), CustomerOrder.class).getResultList();
//
//        CustomerOrderListDto[] dtos = mapper.map(list, CustomerOrderListDto[].class);
//        dtoList = Arrays.asList(dtos);
//        return dtoList;
//    }

    public SingleCustomerOrderDto findById(String authHeader, Long customerOrderId) throws Exception {
        Optional<CustomerOrder> optionalCustomerOrder = customerOrderRepository.findById(customerOrderId);
        if (!optionalCustomerOrder.isPresent()) {
            throw new Exception("Sipariş kaydı bulunamadı");
        }
        SingleCustomerOrderDto dto = mapper.map(optionalCustomerOrder.get(), SingleCustomerOrderDto.class);
        return dto;
    }

    public List<CustomerOrderSearchListDto> getAllPricingPhase() {
        Optional<CustomerOrderStatus> customerOrderStatus = orderStatusRepository.findById(2l);
        List<CustomerOrder> orders = customerOrderRepository.findByStatusAndOrderStatusOrderByCustomerOrderId(1, customerOrderStatus.get());
        CustomerOrderSearchListDto[] array = mapper.map(orders, CustomerOrderSearchListDto[].class);
        List<CustomerOrderSearchListDto> dtos = Arrays.asList(array);
        return dtos;
    }
    public List<CustomerOrderSearchListDto> getAllApprovePhase() {
        Optional<CustomerOrderStatus> customerOrderStatus = orderStatusRepository.findById(10l);
        List<CustomerOrder> orders = customerOrderRepository.findByStatusAndOrderStatusOrderByCustomerOrderId(1, customerOrderStatus.get());
        CustomerOrderSearchListDto[] array = mapper.map(orders, CustomerOrderSearchListDto[].class);
        List<CustomerOrderSearchListDto> dtos = Arrays.asList(array);
        return dtos;
    }
    public List<CustomerOrderDrugsDto> getCustomerOrderDrugList(Long customerOrderId) {
        Optional<CustomerOrderStatus> customerOrderStatus = orderStatusRepository.findById(10l);
        Optional<CustomerOrder> order = customerOrderRepository.findById(customerOrderId);
        CustomerOrderDrugsDto[] array = mapper.map(order.get().getCustomerOrderDrugs(), CustomerOrderDrugsDto[].class);
        List<CustomerOrderDrugsDto> dtos = Arrays.asList(array);
        return dtos;
    }
    public Boolean approveCustomerOrder(Long customerOrderId) throws Exception{
        Optional<CustomerOrderStatus> customerOrderStatus = orderStatusRepository.findById(14l);
        Optional<CustomerOrder> order = customerOrderRepository.findById(customerOrderId);
        CustomerOrder customerOrder = order.get();
        if(customerOrder.getOrderStatus().getOrderStatusId() != 10){
            return false;
        }
        customerOrder.setOrderStatus(customerOrderStatus.get());
        if(!customerOrderStatusHistoryService.save(customerOrder,customerOrderStatus.get()))
            throw new Exception("Sipariş Oluşturulamadı");
        customerOrder = customerOrderRepository.save(customerOrder);
        return true;
    }


    public Boolean customerOrderUpdateCurrencyFee(String authHeader, CustomerOrderUpdateDto dto) throws Exception {

        try {
            User user = this.getUserFromToken(authHeader);
            Optional<CustomerOrder> optionalCustomerOrder = customerOrderRepository.findById(dto.getCustomerOrderId());
            if (!optionalCustomerOrder.isPresent()) {
                throw new Exception("Sipariş kaydı bulunamadı");
            }
            CustomerOrder customerOrder = optionalCustomerOrder.get();
            customerOrder.setCurrencyFee(dto.getCurrencyFee());
            customerOrder = customerOrderRepository.save(customerOrder);
            if(customerOrder.getCustomerOrderDrugs().size()>0)
            for (CustomerOrderDrugs drugs : customerOrder.getCustomerOrderDrugs()){
                if(drugs.getCurrencyFee() == null){
                    drugs.setCurrencyFee(dto.getCurrencyFee());
                    drugs = customerOrderDrugsRepository.save(drugs);
                }
                if(drugs.getCurrencyFee() == 1){
                    drugs.setCurrencyFee(dto.getCurrencyFee());
                    drugs = customerOrderDrugsRepository.save(drugs);
                }
            }
            return true;

        } catch (Exception e) {
            throw e;
        }
    }


    public Boolean customerOrderUpdate(String authHeader, CustomerOrderUpdateDto dto) throws Exception {

        try {
            User user = this.getUserFromToken(authHeader);
            Optional<CustomerOrder> optionalCustomerOrder = customerOrderRepository.findById(dto.getCustomerOrderId());
            if (!optionalCustomerOrder.isPresent()) {
                throw new Exception("Sipariş kaydı bulunamadı");
            }
            CustomerOrder customerOrder = optionalCustomerOrder.get();
            customerOrder.setCurrencyType(dto.getCurrencyType());
            customerOrder.setFreightCostTl(dto.getFreightCostTl());
            customerOrder.setPaymentTerms(dto.getPaymentTerms());
            customerOrder.setLeadTime(dto.getLeadTime());
            customerOrder.setDeliveryTerms(dto.getDeliveryTerms());
            customerOrder.setAdditionalDetails(dto.getAdditionalDetails());
            for(CustomerOrderDrugs drugs : customerOrder.getCustomerOrderDrugs()){
                drugs.setCurrencyType(customerOrder.getCurrencyType());
                drugs = customerOrderDrugsRepository.save(drugs);
            }
            customerOrder = customerOrderRepository.save(customerOrder);
            return true;

        } catch (Exception e) {
            throw e;
        }
    }


    public Boolean saveOrderNote(String authHeader, CustomerOrderDto dto) throws Exception {
        try {
            User user = this.getUserFromToken(authHeader);
            CustomerOrder customerOrder = null;
            Optional<CustomerOrder> optionalCustomerOrder = null;
            if (user.getRole() == Role.ADMIN) {
                optionalCustomerOrder = customerOrderRepository.findById(dto.getCustomerOrderId());
                if (!optionalCustomerOrder.isPresent())
                    throw new NotFoundException("Sipariş Kaydı bulunamadı");

            } else if (user.getRole() == Role.EXPORTER) {
                optionalCustomerOrder = customerOrderRepository.findByCustomerOrderIdAndUser(
                        dto.getCustomerOrderId(),
                        user);
                if (!optionalCustomerOrder.isPresent())
                    throw new NotFoundException("Sipariş Kaydı bulunamadı");

            }
            customerOrder = optionalCustomerOrder.get();
            customerOrder.setCustomerOrderNote(dto.getCustomerOrderNote());
            customerOrder = customerOrderRepository.save(customerOrder);
            return true;
        } catch (Exception e) {
            throw e;
        }
    }


    public Boolean setOfferApprovalStage(String authHeader, CustomerOrderSaveDto dto) throws Exception {
        Boolean control = true;
        User user = this.getUserFromToken(authHeader);
        Optional<CustomerOrder> opt = customerOrderRepository.findById(dto.getCustomerOrderId());
        if (!opt.isPresent()) {
            throw new NotFoundException("Sipariş Kaydı bulunamadı");
        }
        CustomerOrder customerOrder = opt.get();
        if (customerOrder.getCurrencyFee() == null && customerOrder.getCurrencyFee() == 0) {
            throw new NotFoundException("Döviz kurunu belirleyiniz");
        }

        if (customerOrder.getCurrencyFee() == null)
            return false;
        if (customerOrder.getCurrencyFee() == 0)
            return false;
        List<CustomerOrderDrugs> customerOrderDrugsList = customerOrder.getCustomerOrderDrugs();
        for (CustomerOrderDrugs drug : customerOrderDrugsList) {
            if(drug.getIsDeleted() == 0 ){
                if (drug.getUnitPrice() == null) return false;
                if (drug.getUnitPrice() == 0) return false;
            }
        }
        if (control) {
            Optional<CustomerOrderStatus> customerOrderStatus = orderStatusRepository.findById(7l);
            customerOrder.setOrderStatus(customerOrderStatus.get());
            if(!customerOrderStatusHistoryService.save(customerOrder,customerOrderStatus.get()))
                throw new Exception("Sipariş Oluşturulamadı");
            customerOrderRepository.save(customerOrder);
        }
        return control;

    }
    public Boolean setBackOfferApprovalStage(String authHeader, Long customerOrderId) throws Exception {
        Optional<CustomerOrderStatus> customerOrderStatus = orderStatusRepository.findById(7l);
        Optional<CustomerOrder> order = customerOrderRepository.findById(customerOrderId);
        CustomerOrder customerOrder = order.get();
        if(customerOrder.getOrderStatus().getOrderStatusId() != 10){
            return false;
        }
        customerOrder.setOrderStatus(customerOrderStatus.get());
        if(!customerOrderStatusHistoryService.save(customerOrder,customerOrderStatus.get()))
            throw new Exception("Sipariş Oluşturulamadı");
        customerOrder = customerOrderRepository.save(customerOrder);
        return true;

    }
    public Boolean sendToSupplierOrderSearch(String authHeader, CustomerOrderSaveDto dto) throws Exception {

        Boolean control = true;
        User user = this.getUserFromToken(authHeader);
        CustomerOrder customerOrder = null;
        Optional<CustomerOrderStatus> customerOrderStatus = null;
        Optional<CustomerOrder> opt= null;
        if (user.getRole() == Role.ADMIN) {
            opt = customerOrderRepository.findById(dto.getCustomerOrderId());
            if (!opt.isPresent()) {
                throw new NotFoundException("Sipariş Kaydı bulunamadı");
            }
        } else if (user.getRole() == Role.EXPORTER) {
            opt = customerOrderRepository.findByCustomerOrderIdAndUser(dto.getCustomerOrderId(), user);
            if (!opt.isPresent()) {
                throw new NotFoundException("Sipariş Kaydı bulunamadı");
            }
        }
        customerOrder = opt.get();
        customerOrderStatus = orderStatusRepository.findById(15l);
        if(customerOrder.getOrderStatusHistory() != null){
            if(customerOrder.getOrderStatusHistory()>15 && customerOrder.getOrderStatusHistory()<50){
                customerOrderStatus = orderStatusRepository.findById(customerOrder.getOrderStatusHistory());
                customerOrder.setOrderStatusHistory(null);
            }else if(customerOrder.getOrderStatusHistory()>49){
                customerOrderStatus = orderStatusRepository.findById(40L);
                customerOrder.setOrderStatusHistory(null);
            }
        }
        customerOrder.setOrderStatus(customerOrderStatus.get());
        if(!customerOrderStatusHistoryService.save(customerOrder,customerOrderStatus.get()))
            throw new Exception("Sipariş Oluşturulamadı");
        customerOrder = customerOrderRepository.save(customerOrder);
        this.purchaseSetOrders(customerOrder);
        return control;
    }

    public void purchaseSetOrders(CustomerOrder customerOrder) {

        for (CustomerOrderDrugs customerOrderDrugs : customerOrder.getCustomerOrderDrugs()) {
            if(customerOrderDrugs.getPurchaseOrderStatus() == 1 || customerOrderDrugs.getIsDeleted() == 1  )
                continue;
            if(customerOrderDrugs.getUnitPrice()== 0.0)
                continue;
            PurchaseOrderDrugs purchaseOrderDrugs = new PurchaseOrderDrugs();

            purchaseOrderDrugs.setPurchaseStatus(purchaseStatusRepository.findById(10L).get());//10 sipariş hiç verilmemiş durumu
            purchaseOrderDrugs.setTotalQuantity(customerOrderDrugs.getTotalQuantity());
            purchaseOrderDrugs.setChargedQuantity(0L);
            purchaseOrderDrugs.setIncompleteQuantity(purchaseOrderDrugs.getTotalQuantity() - purchaseOrderDrugs.getChargedQuantity());
            purchaseOrderDrugs.setExpirationDate(customerOrderDrugs.getExpirationDate());
            purchaseOrderDrugs.setDrugCard(customerOrderDrugs.getDrugCard());
            purchaseOrderDrugs.setCustomerOrder(customerOrderDrugs.getCustomerOrder());
            purchaseOrderDrugs.setExporterUnitPrice(customerOrderDrugs.getUnitPrice().floatValue());
            purchaseOrderDrugs.setPurchaseOrderDrugAdminNote(customerOrderDrugs.getPurchaseOrderDrugAdminNote());
            purchaseOrderDrugs = purchaseOrderDrugsRepository.save(purchaseOrderDrugs);
            customerOrderDrugs.setPurchaseOrderStatus(1);
            customerOrderDrugs.setPurchaseOrderDrugsId(purchaseOrderDrugs.getPurchaseOrderDrugsId());
            customerOrderDrugs = customerOrderDrugsRepository.save(customerOrderDrugs);
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



    public CustomerOrderTotalPackagingDataDto getTotalPackagingData(String authHeader, CustomerOrderSaveDto dto) throws Exception {
        Optional<Object> result = null;
        User user = this.getUserFromToken(authHeader);
        if (user.getRole() == Role.ADMIN) {
            result = customerOrderRepository.getTotalPackagingDataForAdmın(dto.getCustomerOrderId());

        } else if (user.getRole() == Role.EXPORTER) {
            result = customerOrderRepository.getTotalPackagingDataForExporter(dto.getCustomerOrderId(), user.getUserId());

        }
        CustomerOrderTotalPackagingDataDto dtoTotal = new CustomerOrderTotalPackagingDataDto();
        if (result.isPresent()) {
            Object[] array = (Object[]) result.get();
            dtoTotal.setCustomerOrderId(Long.valueOf(array[0].toString()));
            dtoTotal.setChargedQuantity(Long.valueOf(array[1].toString()));
            dtoTotal.setIncompleteQuantity(Long.valueOf(array[2].toString()));
            dtoTotal.setTotalQuantity(Long.valueOf(array[3].toString()));
        }
        return dtoTotal;
    }
    public Boolean sendToApproveToManager(String authHeader, Long customerId, Long customerOrderId) throws Exception {
        User user = this.getUserFromToken(authHeader);
        Optional<CustomerOrder> optionalCustomerOrder= null;
        Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
        if (user.getRole() == Role.ADMIN) {
            optionalCustomerOrder = customerOrderRepository.getSingleCustomerOrder(customerOrderId, optionalCustomer.get().getCustomerId());

        }else if (user.getRole() == Role.EXPORTER){
            optionalCustomerOrder = customerOrderRepository.getSingleCustomerOrderForExporter(customerOrderId, optionalCustomer.get().getCustomerId(), user.getUserId());

        }
        if(!optionalCustomerOrder.isPresent())
            throw new Exception("Böyle bir sipariş yoktur");
        CustomerOrder customerOrder= optionalCustomerOrder.get();
        Optional<CustomerOrderStatus> customerOrderStatus = orderStatusRepository.findById(10l);
        customerOrder.setOrderStatus(customerOrderStatus.get());
        if(!customerOrderStatusHistoryService.save(customerOrder,customerOrderStatus.get()))
            throw new Exception("Sipariş Oluşturulamadı");
        customerOrderRepository.save(customerOrder);
        return true;
    }
    public String createQuotationFormPdf(String authHeader, Long customerId, Long customerOrderId) throws Exception {
        try {
            User user = this.getUserFromToken(authHeader);
            Optional<CustomerOrder> optionalCustomerOrder= null;
            Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
            if (user.getRole() == Role.ADMIN) {
                optionalCustomerOrder = customerOrderRepository.getSingleCustomerOrder(customerOrderId, optionalCustomer.get().getCustomerId());

            }else if (user.getRole() == Role.EXPORTER){
                optionalCustomerOrder = customerOrderRepository.getSingleCustomerOrderForExporter(customerOrderId, optionalCustomer.get().getCustomerId(), user.getUserId());

            }
            if(!optionalCustomerOrder.isPresent())
                throw new Exception("Böyle bir sipariş yoktur");
            CustomerOrder customerOrder= optionalCustomerOrder.get();

            //PDF BAŞLANGIÇ
            String fileName = customerOrder.getCustomerOrderNo()+"-"+user.getUserId()+".pdf";
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("docs/"+fileName));
            document.open();

            Image image1 = Image.getInstance("docs/pharma.png");
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

            Paragraph tableHeaderOrderCustomer = new Paragraph("Müşteri Bilgileri", catFont);
            tableHeaderOrderCustomer.setAlignment(Element.ALIGN_CENTER);
            document.add(tableHeaderOrderCustomer);

            document.add(new Paragraph("\n"));

            PdfPTable tableOrderCustomer = new PdfPTable(2);
            tableOrderCustomer.setWidths(new int[]{6, 6});

            tableOrderCustomer.setWidthPercentage(100);
            addTableOrderCustomer(tableOrderCustomer);

            addRows(tableOrderCustomer, optionalCustomer.get().getName());
            addRows(tableOrderCustomer, optionalCustomer.get().getSurname());

            document.add(tableOrderCustomer);
            document.add(new Paragraph("\n"));
            //
            Paragraph tableHeaderOrder = new Paragraph("Sipariş Bilgileri", catFont);
            tableHeaderOrder.setAlignment(Element.ALIGN_CENTER);
            document.add(tableHeaderOrder);

            document.add(new Paragraph("\n"));

            PdfPTable tableOrder = new PdfPTable(3);
            tableOrder.setWidths(new int[]{6, 6,  6,});

            tableOrder.setWidthPercentage(100);
            addTableHeaderOrder(tableOrder);

            addRows(tableOrder, optionalCustomerOrder.get().getCustomerOrderNo());
            addRows(tableOrder, optionalCustomerOrder.get().getUser().getName() + " " + optionalCustomerOrder.get().getUser().getSurname());
            addRows(tableOrder, dateFormat.format(optionalCustomerOrder.get().getOrderDate()));

            document.add(tableOrder);
            document.add(new Paragraph("\n"));
            //Tablonun Başlığı Girilir
            Paragraph tableHeader = new Paragraph("Sipariş İlaç Listesi", catFont);

            tableHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(tableHeader);

            document.add(new Paragraph("\n"));

            //Tablonun Sütun Sayısı Girilir
            PdfPTable table = new PdfPTable(6);
            table.setWidths(new int[]{3, 10,  4,4, 4, 4});

            table.setWidthPercentage(100);
            addTableHeader(table);

            //Hücrelere Veriler Girilir
            int a = 0;
            for (CustomerOrderDrugs drug : customerOrder.getCustomerOrderDrugs()) {
                a++;
                addRows(table, String.valueOf(a));
                addRows(table, drug.getDrugCard().getDrugName());
                if(drug.getUnitPrice() != null) addRows(table, drug.getUnitPrice().toString()+" TL");
                else addRows(table, "");
                if(drug.getUnitPrice() != null && drug.getCustomerOrder().getCurrencyFee() != null)
                    if(drug.getUnitPrice() != 0 && drug.getCustomerOrder().getCurrencyFee() != 0){
                        Double calculate = (drug.getUnitPrice()/drug.getCustomerOrder().getCurrencyFee());
                        Double unitCost = ( (double) ( (int) (calculate * 1000.0) ) ) / 1000.0 ;
                        addRows(table,unitCost.toString()+" \n"+drug.getCustomerOrder().getCurrencyType() );
                    }else
                        addRows(table,"");
                else
                    addRows(table,"");
                if(drug.getExpirationDate() != null) addRows(table, dateFormat.format(drug.getExpirationDate()));
                else addRows(table, "");
                if(drug.getTotalQuantity() != null) addRows(table, drug.getTotalQuantity().toString()+" Adet");
                else addRows(table, "");

            }

            document.add(table);
            document.close();
            //PDF SON
            return fileName;
        } catch (Exception e){
            throw new Exception("Müşteri Siparişi Pdf Oluşturma İşleminde Hata Oluştu.",e);
        }
    }

    private void addTableOrderCustomer(PdfPTable table) throws IOException, DocumentException {
        BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
        Font catFont = new Font(bf, 10, Font.NORMAL, BaseColor.BLACK);

        //Tablo Sütün Başlıkları Girilir
        Stream.of("Müşteri Adı", "Müşteri Soyadı")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(1);
                    header.setPhrase(new Phrase(columnTitle, catFont));
                    header.setPadding(3);
                    table.addCell(header);
                });
    }

    private void addTableHeaderOrder(PdfPTable table) throws IOException, DocumentException {
        BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
        Font catFont = new Font(bf, 10, Font.NORMAL, BaseColor.BLACK);

        //Tablo Sütün Başlıkları Girilir
        Stream.of("Sipariş No", "Siparişi Oluşturan", "Siparişin Oluşturulma Tarihi")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(1);
                    header.setPhrase(new Phrase(columnTitle, catFont));
                    header.setPadding(3);


                    table.addCell(header);

                });
    }


    private void addTableHeader(PdfPTable table) throws IOException, DocumentException {
        BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1254", BaseFont.EMBEDDED);
        Font catFont = new Font(bf, 10, Font.NORMAL, BaseColor.BLACK);

        //Tablo Sütün Başlıkları Girilir
        Stream.of("No", "İlaç Adı", "Birim Satış Fiyatı", "Döviz Kurlu Birim Satış Fiyatı", "SKT", "Miktar")
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




    public String quotationForm(String authHeader, Long customerId, Long customerOrderId) throws Exception {

        try {
            Optional<Object> result = null;
            User user = this.getUserFromToken(authHeader);
            Optional<CustomerOrder> optionalCustomerOrder= null;
            Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
            if(!optionalCustomer.isPresent())
                throw new Exception("Böyle Bir Müşteri Yoktur");
            if (user.getRole() == Role.ADMIN || user.getRole() == Role.PURCHASE ) {
                optionalCustomerOrder = customerOrderRepository.getSingleCustomerOrder(customerOrderId, optionalCustomer.get().getCustomerId());
                if (!optionalCustomerOrder.isPresent())
                    throw new Exception("Böyle Bir Sipariş Yoktur");
            } else if (user.getRole() == Role.EXPORTER){
                optionalCustomerOrder = customerOrderRepository.getSingleCustomerOrderForExporter(customerOrderId, optionalCustomer.get().getCustomerId(), user.getUserId());
                if (!optionalCustomerOrder.isPresent())
                    throw new Exception("Böyle Bir Sipariş Yoktur");

            }
            CustomerOrder customerOrder = optionalCustomerOrder.get();
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Quotation Form");

            //set header
            createHeaderForExcel(workbook, sheet, "QUOTATION FORM");

            //set logo
            createLogoForExcel(workbook, sheet);

            //set border
            XSSFCellStyle styleBorderLeft = setRowProperties(workbook,HorizontalAlignment.LEFT,VerticalAlignment.CENTER, true);
            styleBorderLeft.setBorderLeft(BorderStyle.MEDIUM);
            XSSFCellStyle styleBorderBottom = setRowProperties(workbook,HorizontalAlignment.CENTER,VerticalAlignment.CENTER, true);
            styleBorderBottom.setBorderBottom(BorderStyle.MEDIUM);
            XSSFCellStyle styleBorderRight = setRowProperties(workbook,HorizontalAlignment.CENTER,VerticalAlignment.CENTER, true);
            styleBorderRight.setBorderRight(BorderStyle.MEDIUM);


            //row 1
            XSSFRow row1 = sheet.createRow((short) 1);
            row1.createCell(7).setCellValue("");
            row1.getCell(7).setCellStyle(styleBorderRight);

            //row 2
            XSSFRow row2 = sheet.createRow((short) 2);
            row2.createCell(7).setCellValue("");
            row2.getCell(7).setCellStyle(styleBorderRight);

            //row 5
            XSSFRow row5 = sheet.createRow((short) 5);
            row5.createCell(7).setCellValue("");
            row5.getCell(7).setCellStyle(styleBorderRight);

            //row 6
            XSSFRow row6 = sheet.createRow((short) 6);
            row6.createCell(7).setCellValue("");
            row6.getCell(7).setCellStyle(styleBorderRight);

            //row 7
            XSSFRow row7 = sheet.createRow((short) 7);
            row7.createCell(7).setCellValue("");
            row7.getCell(7).setCellStyle(styleBorderRight);

            //row 8
            XSSFRow row8 = sheet.createRow((short) 8);
            row8.createCell(7).setCellValue("");
            row8.getCell(7).setCellStyle(styleBorderRight);

            //row 3
            XSSFRow row3 = sheet.createRow((short) 3);
            row3.createCell(2).setCellValue("Reference No : "+customerOrder.getCustomerOrderNo());
            row3.getCell(2).setCellStyle(setRowProperties(workbook,HorizontalAlignment.CENTER, VerticalAlignment.CENTER, true));

            row3.createCell(7).setCellValue("");
            row3.getCell(7).setCellStyle(styleBorderRight);

            //row3
            row3.createCell(5).setCellValue("Customer Name : " );
            row3.getCell(5).setCellStyle(setRowProperties(workbook,HorizontalAlignment.CENTER, VerticalAlignment.CENTER, true));

            //row3
            row3.createCell(6).setCellValue(customerOrder.getCustomer().getName()+" "+customerOrder.getCustomer().getSurname());
            row3.getCell(6).setCellStyle(setRowProperties(workbook,HorizontalAlignment.CENTER, VerticalAlignment.CENTER, true));

            //row4
            XSSFRow row4 = sheet.createRow((short) 4);
            row4.createCell(2).setCellValue("Date : "+customerOrder.getOrderDate());
            row4.getCell(2).setCellStyle(setRowProperties(workbook,HorizontalAlignment.CENTER, VerticalAlignment.CENTER, true));

            row4.createCell(7).setCellValue("");
            row4.getCell(7).setCellStyle(styleBorderRight);

            if(customerOrder.getCompany() != null){
                row4.createCell(5).setCellValue("Company Name : ");
                row4.getCell(5).setCellStyle(setRowProperties(workbook,HorizontalAlignment.CENTER, VerticalAlignment.CENTER, true));
                row4.createCell(6).setCellValue(customerOrder.getCompany().getCompanyName());
                row4.getCell(6).setCellStyle(setRowProperties(workbook,HorizontalAlignment.CENTER, VerticalAlignment.CENTER, true));
            }


            sheet.setColumnWidth(0, 1500);
            sheet.setColumnWidth(1, 5500);
            sheet.setColumnWidth(2, 9000);
            sheet.setColumnWidth(3, 2500);
            sheet.setColumnWidth(4, 3000);
            sheet.setColumnWidth(5, 4000);
            sheet.setColumnWidth(6, 8000);
            sheet.setColumnWidth(7, 4000);


            int row = 7;
            row++;
            XSSFRow rowHeader = sheet.createRow(row);
            createSmallHeaderForExcel(workbook, sheet,"",rowHeader, 0);
            sheet.addMergedRegion(new CellRangeAddress(row,row,1,2));
            createSmallHeaderForExcel(workbook, sheet,"PRODUCTS",rowHeader, 1);
            //createSmallHeaderForExcel(workbook, sheet,"PRODUCTS",rowHeader, 2);
            createSmallHeaderForExcel(workbook, sheet,"QTY - PACK",rowHeader, 3);
            createSmallHeaderForExcel(workbook, sheet,"MIN EXP.DATE",rowHeader, 4);
            createSmallHeaderForExcel(workbook, sheet,"PRICE-"+customerOrder.getCurrencyType(),rowHeader, 5);
            createSmallHeaderForExcel(workbook, sheet,"MANUFACTURER",rowHeader, 6);
            createSmallHeaderForExcel(workbook, sheet,"TOTAL PRICE-"+customerOrder.getCurrencyType(),rowHeader, 7);
            List<CustomerOrderDrugs> drugs = customerOrder.getCustomerOrderDrugs();
            row++;
            int count=0;

            //[$$-en-US]#.##0,00 dolar
            //#.##0,00 [$€-de-DE]  euro
            //[$£-cy-GB]#.##0,00   sterlin
            //#.##0,00 ₺ tl
            String currencyLogo ="";

            String logo = "";
            if(customerOrder.getCurrencyType() == CurrencyType.USD)         logo = "$";
            if(customerOrder.getCurrencyType() == CurrencyType.EURO)        logo = "€";
            if(customerOrder.getCurrencyType()== CurrencyType.STERLIN)      logo = "£";
            if(customerOrder.getCurrencyType() == CurrencyType.TL)          logo = "₺";

            Double totalOrderPrice = 0.0;
            for (CustomerOrderDrugs drug :drugs) {

                count++;
                sheet.addMergedRegion(new CellRangeAddress(row,row,1,2));
                XSSFRow newRow = sheet.createRow((short) row );
                newRow.setHeight((short)600);


                newRow.createCell(0).setCellValue(count);
                newRow.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.CENTER, VerticalAlignment.CENTER, false));

                newRow.createCell(1).setCellValue(drug.getDrugCard().getDrugName());
                newRow.getCell(1).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));

                newRow.createCell(3).setCellValue(drug.getTotalQuantity());
                newRow.getCell(3).setCellStyle(setRowProperties(workbook,HorizontalAlignment.CENTER, VerticalAlignment.CENTER, false));

                newRow.createCell(4).setCellValue(dateFormat.format(drug.getExpirationDate()));
                newRow.getCell(4).setCellStyle(setRowProperties(workbook,HorizontalAlignment.CENTER, VerticalAlignment.CENTER, false));


                if(drug.getUnitPrice() != null && (drug.getCurrencyFee() != null) ){
                    Double currencyFee = 1.0;
                    if( drug.getCurrencyFee() != 0)
                        currencyFee = drug.getCurrencyFee();
                    Double calculate = (drug.getUnitPrice()/currencyFee);
                    Double unitPrice = ( (double) ( (int) (calculate * 100.0) ) ) / 100.0 ;

                    newRow.createCell(5).setCellValue(logo+""+unitPrice.toString());
                    newRow.getCell(5).setCellStyle(createStyleWithCurrencyLogo(workbook, customerOrder.getCurrencyType()));
                }
                else {
                    newRow.createCell(5).setCellValue(logo+"");
                    newRow.getCell(5).setCellStyle(createStyleWithCurrencyLogo(workbook, customerOrder.getCurrencyType()));
                }

                newRow.createCell(6).setCellValue(drug.getDrugCard().getDrugCompany());
                newRow.getCell(6).setCellStyle(setRowProperties(workbook,HorizontalAlignment.CENTER, VerticalAlignment.CENTER, false));

                if(drug.getTotalQuantity() != null && drug.getUnitPrice() != null && drug.getCurrencyFee() != null){
                    Double currencyFee = 1.0;
                    if( drug.getCurrencyFee() != 0)
                        currencyFee = drug.getCurrencyFee();
                    Double calculate2 = ((drug.getTotalQuantity()*drug.getUnitPrice())/currencyFee);
                    Double totalPrice = ( (double) ( (int) (calculate2 * 100.0) ) ) / 100.0 ;
                    totalOrderPrice += totalPrice;
                    newRow.createCell(7).setCellValue(logo+""+totalPrice.toString());
                    newRow.getCell(7).setCellStyle(createStyleWithCurrencyLogo(workbook, customerOrder.getCurrencyType(),BorderTypeEnum.RIGHT));
                }
                else{
                    newRow.createCell(7).setCellValue(logo+"");
                    newRow.getCell(7).setCellStyle(createStyleWithCurrencyLogo(workbook, customerOrder.getCurrencyType(),BorderTypeEnum.RIGHT));
                }
                //newRow.createCell(7).setCellFormula("D"+(row+1)+"*F"+(row+1));

                row++;
            }

            XSSFRow nullHeaderRow = sheet.createRow(row);
            sheet.addMergedRegion(new CellRangeAddress(row,row,0,7));
            createSmallHeaderForExcel(workbook, sheet,"",nullHeaderRow, 0);
            row++;

            XSSFRow rowCustomCost = sheet.createRow((short) row);
            rowCustomCost.createCell(6).setCellValue("TOTAL AS EXW - "+customerOrder.getCurrencyType());
            rowCustomCost.getCell(6).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));
            if(totalOrderPrice != null){
                Double top = ( (double) ( (int) (totalOrderPrice * 1000.0) ) ) / 1000.0 ;
                rowCustomCost.createCell(7).setCellValue(logo+""+top);
            }
            else
                rowCustomCost.createCell(7).setCellValue(logo+"");
            rowCustomCost.getCell(7).setCellStyle(createStyleWithCurrencyLogo(workbook, customerOrder.getCurrencyType(),BorderTypeEnum.RIGHT));
            row++;


            nullHeaderRow = sheet.createRow(row);
            sheet.addMergedRegion(new CellRangeAddress(row,row,0,7));
            if(user.getEmail()!=null)
                createSmallHeaderForExcel(workbook, sheet,"www.ekippharma.com  -  "+user.getEmail(),nullHeaderRow, 0);
            else
                createSmallHeaderForExcel(workbook, sheet,"www.ekippharma.com  -  ", nullHeaderRow, 0);
            row++;

            XSSFRow rowPaymentTerms = sheet.createRow((short) row);
            rowPaymentTerms.createCell(0).setCellValue("Payment Terms : ");
            rowPaymentTerms.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,3));

            rowPaymentTerms.createCell(7).setCellValue("");
            rowPaymentTerms.getCell(7).setCellStyle(styleBorderRight);
            row++;

            XSSFRow rowDeliveryTerms = sheet.createRow((short) row);
            rowDeliveryTerms.createCell(0).setCellValue("Delivery Terms : ");
            rowDeliveryTerms.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,3));
//
            rowDeliveryTerms.createCell(7).setCellValue("");
            rowDeliveryTerms.getCell(7).setCellStyle(styleBorderRight);
            row++;

            XSSFRow rowLeadTime = sheet.createRow((short) row);
            rowLeadTime.createCell(0).setCellValue("Lead Time : ");
            rowLeadTime.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,3));

            if(customerOrder.getCustomerOrderBankDetail() != null) {
                rowLeadTime.createCell(6).setCellValue(customerOrder.getCustomerOrderBankDetail().getBankName());
                rowLeadTime.getCell(6).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));
            }

            rowLeadTime.createCell(7).setCellValue("");
            rowLeadTime.getCell(7).setCellStyle(styleBorderRight);
            row++;

            XSSFRow rowBlaBla = sheet.createRow((short) row);
            rowBlaBla.createCell(0).setCellValue("Packaging:Unless the buyer states otherwise,products will be packaged according to GDP");
            rowBlaBla.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,3));

            if(customerOrder.getCustomerOrderBankDetail() != null){
                rowBlaBla.createCell(6).setCellValue("");
                rowBlaBla.getCell(6).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));
            }

            rowBlaBla.createCell(7).setCellValue("");
            rowBlaBla.getCell(7).setCellStyle(styleBorderRight);
            row++;

            XSSFRow rowPackages = sheet.createRow((short) row);
            rowPackages.createCell(0).setCellValue("Packages and Leaflets are in Turkish ");
            rowPackages.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,3));

            if(customerOrder.getCustomerOrderBankDetail() != null){
                rowPackages.createCell(6).setCellValue(customerOrder.getCustomerOrderBankDetail().getSwift());
                rowPackages.getCell(6).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));
            }

            rowPackages.createCell(7).setCellValue("");
            rowPackages.getCell(7).setCellStyle(styleBorderRight);
            row++;

            XSSFRow rowOrdersUnder = sheet.createRow((short) row);
            rowOrdersUnder.createCell(0).setCellValue("Orders under 7.500 USD will be charged 150 USD for customs fees ");
            rowOrdersUnder.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,3));

            rowOrdersUnder.createCell(7).setCellValue("");
            rowOrdersUnder.getCell(7).setCellStyle(styleBorderRight);
            row++;

            XSSFRow rowAdditionalDetails = sheet.createRow((short) row);
            rowAdditionalDetails.createCell(0).setCellValue("Additional Details: ");
            rowAdditionalDetails.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,3));


            rowAdditionalDetails.createCell(7).setCellValue("");
            rowAdditionalDetails.getCell(7).setCellStyle(styleBorderRight);
            row++;


            //Firma Bilgileri ve Müşteri Bilgilerinin Oluşturulması
            nullHeaderRow = sheet.createRow(row);
            sheet.addMergedRegion(new CellRangeAddress(row,row,0,7));
            createSmallHeaderForExcel(workbook, sheet,"",nullHeaderRow, 0);
            row++;

            XSSFRow rowSeller = sheet.createRow((short) row);
            rowSeller.createCell(0).setCellValue("SELLER:");
            rowSeller.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,1));

            rowSeller.createCell(4).setCellValue("BUYER:");
            rowSeller.getCell(4).setCellStyle(styleBorderLeft);
            sheet.addMergedRegion(new CellRangeAddress(row, row,4,5));

            rowSeller.createCell(7).setCellValue("");
            rowSeller.getCell(7).setCellStyle(styleBorderRight);

            row++;

            XSSFRow rowEkipAddress = sheet.createRow((short) row);
            rowEkipAddress.createCell(0).setCellValue("Ekip Ecza Deposu San ve Tic ltd Sti");
            rowEkipAddress.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,2));

            rowEkipAddress.createCell(3).setCellValue("");
            rowEkipAddress.getCell(3).setCellStyle(styleBorderRight);


            if(customerOrder.getCustomer().getCompany() != null) {
                rowEkipAddress.createCell(4).setCellValue(customerOrder.getCustomer().getCompany().getCompanyName());
                rowEkipAddress.getCell(4).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
                sheet.addMergedRegion(new CellRangeAddress(row, row, 4, 6));
            }else{
                rowEkipAddress.createCell(4).setCellValue(customerOrder.getCustomer().getName()+" "+customerOrder.getCustomer().getSurname());
                rowEkipAddress.getCell(4).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
                sheet.addMergedRegion(new CellRangeAddress(row, row, 4, 6));
            }

            rowEkipAddress.createCell(7).setCellValue("");
            rowEkipAddress.getCell(7).setCellStyle(styleBorderRight);
            row++;

            XSSFRow rowEkipAddress2 = sheet.createRow((short) row);
            rowEkipAddress2.createCell(0).setCellValue("Ostim Mahallesi 1148 Sokak No:32/C ");
            rowEkipAddress2.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,2));

            rowEkipAddress2.createCell(3).setCellValue("");
            rowEkipAddress2.getCell(3).setCellStyle(styleBorderRight);

            if(customerOrder.getCustomer().getCompany() != null) {
                rowEkipAddress2.createCell(4).setCellValue(customerOrder.getCompany().getAddress() + " " + customerOrder.getCompany().getCity());
                rowEkipAddress2.getCell(4).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
                sheet.addMergedRegion(new CellRangeAddress(row, row, 4, 6));
            }else{
                rowEkipAddress2.createCell(4).setCellValue(customerOrder.getCustomer().getOpenAddress());
                rowEkipAddress2.getCell(4).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
                sheet.addMergedRegion(new CellRangeAddress(row, row,4, 6));
                }

            rowEkipAddress2.createCell(7).setCellValue("");
            rowEkipAddress2.getCell(7).setCellStyle(styleBorderRight);
            row++;

            XSSFRow rowIlIlce = sheet.createRow((short) row);
            rowIlIlce.createCell(0).setCellValue("Yenimahalle/Ankara ");
            rowIlIlce.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,1));

            rowIlIlce.createCell(3).setCellValue("");
            rowIlIlce.getCell(3).setCellStyle(styleBorderRight);

            if(customerOrder.getCompany() !=null) {
                rowIlIlce.createCell(4).setCellValue(customerOrder.getCompany().getCountry().getEnglishName());
                rowIlIlce.getCell(4).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
                sheet.addMergedRegion(new CellRangeAddress(row, row, 4, 6));

            }else{ rowIlIlce.createCell(4).setCellValue(customerOrder.getCustomer().getCity()+ " / " + customerOrder.getCustomer().getCountry().getEnglishName());
                rowIlIlce.getCell(4).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
                sheet.addMergedRegion(new CellRangeAddress(row, row, 4, 6));
            }
            rowIlIlce.createCell(7).setCellValue("");
            rowIlIlce.getCell(7).setCellStyle(styleBorderRight);
            row++;


            XSSFRow rowEkipAddressUlke = sheet.createRow((short) row);
            rowEkipAddressUlke.createCell(0).setCellValue("TURKIYE");
            rowEkipAddressUlke.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,2));

            rowEkipAddressUlke.createCell(3).setCellValue("");
            rowEkipAddressUlke.getCell(3).setCellStyle(styleBorderRight);

            rowEkipAddressUlke.createCell(7).setCellValue("");
            rowEkipAddressUlke.getCell(7).setCellStyle(styleBorderRight);
            row++;


            XSSFRow rowBlank2 = sheet.createRow((short) row);
            rowBlank2.createCell(0).setCellValue("");
            rowBlank2.getCell(0).setCellStyle(styleBorderBottom);
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,1));

            rowBlank2.createCell(1).setCellValue("");
            rowBlank2.getCell(1).setCellStyle(styleBorderBottom);

            rowBlank2.createCell(2).setCellValue("");
            rowBlank2.getCell(2).setCellStyle(styleBorderBottom);

            rowBlank2.createCell(3).setCellValue("");
            rowBlank2.getCell(3).setCellStyle(styleBorderBottom);

            rowBlank2.createCell(4).setCellValue("");
            showRowWithBorder(workbook,sheet,"", rowBlank2, 4, BorderTypeEnum.LEFT, BorderTypeEnum.BOTTOM);

            rowBlank2.createCell(5).setCellValue("");
            rowBlank2.getCell(5).setCellStyle(styleBorderBottom);

            rowBlank2.createCell(6).setCellValue("");
            rowBlank2.getCell(6).setCellStyle(styleBorderBottom);

            rowBlank2.createCell(7).setCellValue("");
            showRowWithBorder(workbook,sheet,"", rowBlank2, 7, BorderTypeEnum.RIGHT, BorderTypeEnum.BOTTOM);
            row++;

            //set signature
            createSignatureForExcel(workbook, sheet,row);


            //A4 Sayfaya Sığdırma
            sheet.setFitToPage(true);
            PrintSetup ps = sheet.getPrintSetup();
            ps.setPaperSize(PrintSetup.A4_PAPERSIZE);
            ps.setFitWidth( (short) 1);
            ps.setFitHeight( (short) 0);


            FileOutputStream fileOut = new FileOutputStream(
                    "docs/"+customerOrder.getCustomerOrderNo()+"-"+user.getUserId()+".xlsx");
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
            String fileName = customerOrder.getCustomerOrderNo()+"-"+user.getUserId();

            return fileName;
        }catch (Exception e){
            throw new Exception("Sipariş Excel Oluşturma İşleminde Hata Oluştu.",e);
        }
    }


    public String proformaInvoice(String authHeader, Long customerId, Long customerOrderId) throws Exception {

        try {
            Optional<Object> result = null;
            User user = this.getUserFromToken(authHeader);
            Optional<CustomerOrder> optionalCustomerOrder = null;
            Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
            if (!optionalCustomer.isPresent())
                throw new Exception("Böyle Bir Müşteri Yoktur");
            if (user.getRole() == Role.ADMIN || user.getRole() == Role.PURCHASE) {
                optionalCustomerOrder = customerOrderRepository.getSingleCustomerOrder(customerOrderId, optionalCustomer.get().getCustomerId());
                if (!optionalCustomerOrder.isPresent())
                    throw new Exception("Böyle Bir Sipariş Yoktur");
            } else if (user.getRole() == Role.EXPORTER) {
                optionalCustomerOrder = customerOrderRepository.getSingleCustomerOrderForExporter(customerOrderId, optionalCustomer.get().getCustomerId(), user.getUserId());
                if (!optionalCustomerOrder.isPresent())
                    throw new Exception("Böyle Bir Sipariş Yoktur");

            }
            CustomerOrder customerOrder = optionalCustomerOrder.get();
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Proforma Invoice");

            //set header
            createHeaderForExcel(workbook, sheet, "PROFORMA INVOICE");

            //set logo
            createLogoForExcel(workbook, sheet);

            //set border
            XSSFCellStyle styleBorderLeft = setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true);
            styleBorderLeft.setBorderLeft(BorderStyle.MEDIUM);
            XSSFCellStyle styleBorderBottom = setRowProperties(workbook, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, true);
            styleBorderBottom.setBorderBottom(BorderStyle.MEDIUM);
            XSSFCellStyle styleBorderRight = setRowProperties(workbook, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, true);
            styleBorderRight.setBorderRight(BorderStyle.MEDIUM);

            //Rowların kenar çizgilerini oluşturuyoruz
            //row 1
            XSSFRow row1 = sheet.createRow((short) 1);
            row1.createCell(7).setCellValue("");
            row1.getCell(7).setCellStyle(styleBorderRight);

            //row 2
            XSSFRow row2 = sheet.createRow((short) 2);
            row2.createCell(7).setCellValue("");
            row2.getCell(7).setCellStyle(styleBorderRight);

            //row 5
            XSSFRow row5 = sheet.createRow((short) 5);
            row5.createCell(7).setCellValue("");
            row5.getCell(7).setCellStyle(styleBorderRight);

            //row 6
            XSSFRow row6 = sheet.createRow((short) 6);
            row6.createCell(7).setCellValue("");
            row6.getCell(7).setCellStyle(styleBorderRight);

            //row 7
            XSSFRow row7 = sheet.createRow((short) 7);
            row7.createCell(7).setCellValue("");
            row7.getCell(7).setCellStyle(styleBorderRight);


            //row 3
            XSSFRow row3 = sheet.createRow((short) 3);
            row3.createCell(2).setCellValue("Reference No : " + customerOrder.getCustomerOrderNo());
            row3.getCell(2).setCellStyle(setRowProperties(workbook, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, true));

            //row3
            row3.createCell(5).setCellValue("Customer Name : ");
            row3.getCell(5).setCellStyle(setRowProperties(workbook, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, true));

            //row3
            row3.createCell(6).setCellValue(customerOrder.getCustomer().getName() + " " + customerOrder.getCustomer().getSurname());
            row3.getCell(6).setCellStyle(setRowProperties(workbook, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, true));

            //row3
            row3.createCell(7).setCellValue("");
            row3.getCell(7).setCellStyle(styleBorderRight);

            //row4
            XSSFRow row4 = sheet.createRow((short) 4);
            row4.createCell(2).setCellValue("Date : " + customerOrder.getOrderDate());
            row4.getCell(2).setCellStyle(setRowProperties(workbook, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, true));

            //row4
            row4.createCell(7).setCellValue("");
            row4.getCell(7).setCellStyle(styleBorderRight);

            if (customerOrder.getCompany() != null) {
                row4.createCell(5).setCellValue("Company Name : ");
                row4.getCell(5).setCellStyle(setRowProperties(workbook, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, true));
                row4.createCell(6).setCellValue(customerOrder.getCompany().getCompanyName());
                row4.getCell(6).setCellStyle(setRowProperties(workbook, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, true));
            }

            sheet.setColumnWidth(0, 1500);
            sheet.setColumnWidth(1, 5500);
            sheet.setColumnWidth(2, 9000);
            sheet.setColumnWidth(3, 2500);
            sheet.setColumnWidth(4, 3000);
            sheet.setColumnWidth(5, 4000);
            sheet.setColumnWidth(6, 8000);
            sheet.setColumnWidth(7, 4000);


            int row = 8;
            XSSFRow rowTitle = sheet.createRow((short) row);
            rowTitle.createCell(1).setCellValue("BILL TO :");
            rowTitle.getCell(1).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

            rowTitle.createCell(5).setCellValue("SHIP TO :");
            rowTitle.getCell(5).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

            rowTitle.createCell(7).setCellValue("");
            rowTitle.getCell(7).setCellStyle(styleBorderRight);

            row++;

            XSSFRow rowContent = sheet.createRow((short) row);
            rowContent.createCell(1).setCellValue("Company Name : ");
            rowContent.getCell(1).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            rowContent.createCell(5).setCellValue("Company Name : ");
            rowContent.getCell(5).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

            rowContent.createCell(7).setCellValue("");
            rowContent.getCell(7).setCellStyle(styleBorderRight);

            if (customerOrder.getCustomer().getCompany() != null) {
                rowContent.createCell(2).setCellValue(customerOrder.getCustomer().getCompany().getCompanyName());
                rowContent.getCell(2).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));
                }
                else{
                rowContent.createCell(1).setCellValue(" Customer Name : ");
                rowContent.getCell(1).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

                rowContent.createCell(2).setCellValue(customerOrder.getCustomer().getName() + " " + customerOrder.getCustomer().getSurname());
                rowContent.getCell(2).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));

                rowContent.createCell(5).setCellValue(" Customer Name : ");
                rowContent.getCell(5).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

                rowContent.createCell(6).setCellValue(customerOrder.getCustomer().getName() + " " + customerOrder.getCustomer().getSurname());
                rowContent.getCell(6).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));

            }

            if(customerOrder.getCustomerOrderShippingAdress() != null){
                rowContent.createCell(1).setCellValue("Company Name : ");
                rowContent.getCell(1).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

                rowContent.createCell(2).setCellValue(customerOrder.getCustomerOrderShippingAdress().getCompanyName());
                rowContent.getCell(2).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));

                rowContent.createCell(5).setCellValue("Company Name : ");
                rowContent.getCell(5).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

                rowContent.createCell(6).setCellValue(customerOrder.getCustomerOrderShippingAdress().getCompanyName());
                rowContent.getCell(6).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));
            }
            else
            {   rowContent.createCell(1).setCellValue(" Customer Name : ");
                rowContent.getCell(1).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

                rowContent.createCell(2).setCellValue(customerOrder.getCustomer().getName() + " " + customerOrder.getCustomer().getSurname());
                rowContent.getCell(2).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));

                rowContent.createCell(5).setCellValue(" Customer Name : ");
                rowContent.getCell(5).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

                rowContent.createCell(6).setCellValue(customerOrder.getCustomer().getName() + " " + customerOrder.getCustomer().getSurname());
                rowContent.getCell(6).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));
            }


            row++;

            rowContent = sheet.createRow((short) row);
            rowContent.createCell(1).setCellValue("Company Address : ");
            rowContent.getCell(1).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

            rowContent.createCell(5).setCellValue("Company Address : ");
            rowContent.getCell(5).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

            rowContent.createCell(7).setCellValue("");
            rowContent.getCell(7).setCellStyle(styleBorderRight);

            if(customerOrder.getCustomer().getCompany() != null){
                rowContent.createCell(2).setCellValue(customerOrder.getCustomer().getCompany().getAddress());
                rowContent.getCell(2).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            }

            else{
                rowContent.createCell(1).setCellValue(" Customer Address : ");
                rowContent.getCell(1).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

                rowContent.createCell(2).setCellValue(customerOrder.getCustomer().getOpenAddress());
                rowContent.getCell(2).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

                rowContent.createCell(5).setCellValue(" Customer Address : ");
                rowContent.getCell(5).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

                rowContent.createCell(6).setCellValue(customerOrder.getCustomer().getOpenAddress());
                rowContent.getCell(6).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

            }

            if(customerOrder.getCustomerOrderShippingAdress() != null){
                rowContent.createCell(6).setCellValue(customerOrder.getCustomerOrderShippingAdress().getFullAddress());
                rowContent.getCell(6).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));
            }
            else
                {   rowContent.createCell(1).setCellValue(" Customer Address : ");
                    rowContent.getCell(1).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

                    rowContent.createCell(2).setCellValue(customerOrder.getCustomer().getOpenAddress());
                    rowContent.getCell(2).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));

                    rowContent.createCell(5).setCellValue(" Customer Address : ");
                    rowContent.getCell(5).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

                    rowContent.createCell(6).setCellValue(customerOrder.getCustomer().getOpenAddress());
                    rowContent.getCell(6).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));
                }

            row++;

            rowContent = sheet.createRow((short) row);

            rowContent.createCell(1).setCellValue("City / State : ");
            rowContent.getCell(1).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

            rowContent.createCell(5).setCellValue("City / State : ");
            rowContent.getCell(5).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

            rowContent.createCell(7).setCellValue("");
            rowContent.getCell(7).setCellStyle(styleBorderRight);

            if(customerOrder.getCustomer().getCompany() != null){
                rowContent.createCell(2).setCellValue(customerOrder.getCustomer().getCompany().getCity());
                rowContent.getCell(2).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));
            }
            else
            {   rowContent.createCell(1).setCellValue(" Customer City / State : ");
                rowContent.getCell(1).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

                rowContent.createCell(2).setCellValue(customerOrder.getCustomer().getCity());
                rowContent.getCell(2).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));

                rowContent.createCell(5).setCellValue(" Customer City / State : ");
                rowContent.getCell(5).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

                rowContent.createCell(6).setCellValue(customerOrder.getCustomer().getCity());
                rowContent.getCell(6).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));
            }


            if(customerOrder.getCustomerOrderShippingAdress() != null){
                rowContent.createCell(6).setCellValue(customerOrder.getCustomerOrderShippingAdress().getCity());
                rowContent.getCell(6).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));
            }
            else
            {   rowContent.createCell(1).setCellValue(" Customer City / State : ");
                rowContent.getCell(1).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

                rowContent.createCell(2).setCellValue(customerOrder.getCustomer().getCity());
                rowContent.getCell(2).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));

                rowContent.createCell(5).setCellValue(" Customer City / State : ");
                rowContent.getCell(5).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

                rowContent.createCell(6).setCellValue(customerOrder.getCustomer().getCity());
                rowContent.getCell(6).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));
            }
            row++;

            rowContent = sheet.createRow((short) row);

            rowContent.createCell(1).setCellValue("Country : ");
            rowContent.getCell(1).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

            rowContent.createCell(5).setCellValue("Country : ");
            rowContent.getCell(5).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

            rowContent.createCell(7).setCellValue("");
            rowContent.getCell(7).setCellStyle(styleBorderRight);

            if(customerOrder.getCustomer().getCompany() != null){
                rowContent.createCell(2).setCellValue(customerOrder.getCustomer().getCompany().getCountry().getName());
                rowContent.getCell(2).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));
            }
            else
            {   rowContent.createCell(1).setCellValue(" Customer Country : ");
                rowContent.getCell(1).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

                rowContent.createCell(2).setCellValue(customerOrder.getCustomer().getCountry().getEnglishName());
                rowContent.getCell(2).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));

                rowContent.createCell(5).setCellValue(" Customer Country : ");
                rowContent.getCell(5).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

                rowContent.createCell(6).setCellValue(customerOrder.getCustomer().getCountry().getEnglishName());
                rowContent.getCell(6).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));
            }

            if(customerOrder.getCustomerOrderShippingAdress() != null){
                rowContent.createCell(6).setCellValue(customerOrder.getCustomerOrderShippingAdress().getCountry().getName());
                rowContent.getCell(6).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));
            }
            else
            {   rowContent.createCell(1).setCellValue(" Customer Country : ");
                rowContent.getCell(1).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

                rowContent.createCell(2).setCellValue(customerOrder.getCustomer().getCountry().getEnglishName());
                rowContent.getCell(2).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));

                rowContent.createCell(5).setCellValue(" Customer Country : ");
                rowContent.getCell(5).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

                rowContent.createCell(6).setCellValue(customerOrder.getCustomer().getCountry().getEnglishName());
                rowContent.getCell(6).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));
            }
            row++;

            rowContent = sheet.createRow((short) row);

            rowContent.createCell(1).setCellValue("Phone : ");
            rowContent.getCell(1).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

            rowContent.createCell(5).setCellValue("Phone : ");
            rowContent.getCell(5).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

            rowContent.createCell(7).setCellValue("");
            rowContent.getCell(7).setCellStyle(styleBorderRight);

            if(customerOrder.getCustomer().getCompany() != null){
                rowContent.createCell(2).setCellValue(customerOrder.getCustomer().getCompany().getCompanyPhone());
                rowContent.getCell(2).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));
            }
            else
            {   rowContent.createCell(1).setCellValue(" Customer Phone : ");
                rowContent.getCell(1).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

                rowContent.createCell(2).setCellValue(customerOrder.getCustomer().getMobilePhone());
                rowContent.getCell(2).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));

                rowContent.createCell(5).setCellValue(" Customer Phone : ");
                rowContent.getCell(5).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

                rowContent.createCell(6).setCellValue(customerOrder.getCustomer().getMobilePhone());
                rowContent.getCell(6).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));
            }

            if(customerOrder.getCustomerOrderShippingAdress() != null){
                rowContent.createCell(6).setCellValue(customerOrder.getCustomerOrderShippingAdress().getPhone());
                rowContent.getCell(6).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));
            }
            else
            {   rowContent.createCell(1).setCellValue(" Customer Phone : ");
                rowContent.getCell(1).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

                rowContent.createCell(2).setCellValue(customerOrder.getCustomer().getMobilePhone());
                rowContent.getCell(2).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));

                rowContent.createCell(5).setCellValue(" Customer Phone : ");
                rowContent.getCell(5).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

                rowContent.createCell(6).setCellValue(customerOrder.getCustomer().getMobilePhone());
                rowContent.getCell(6).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));
            }
            row++;

            rowContent = sheet.createRow((short) row);
            rowContent.createCell(1).setCellValue("PL NO : ");
            rowContent.getCell(1).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

            rowContent.createCell(7).setCellValue("");
            rowContent.getCell(7).setCellStyle(styleBorderRight);

            rowContent.createCell(2).setCellValue(customerOrder.getUser().getName()+" "+ customerOrder.getUser().getSurname());
            rowContent.getCell(2).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));

            rowContent.createCell(5).setCellValue("Currency Type : ");
            rowContent.getCell(5).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

            rowContent.createCell(7).setCellValue("");
            rowContent.getCell(7).setCellStyle(styleBorderRight);

            rowContent.createCell(6).setCellValue(customerOrder.getCurrencyType().toString());
            rowContent.getCell(6).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));

            row++;
            XSSFRow rowHeader = sheet.createRow(row);
            createSmallHeaderForExcel(workbook, sheet,"",rowHeader, 0);
            sheet.addMergedRegion(new CellRangeAddress(row,row,1,2));
            createSmallHeaderForExcel(workbook, sheet,"PRODUCTS",rowHeader, 1);
            //createSmallHeaderForExcel(workbook, sheet,"PRODUCTS",rowHeader, 2);
            createSmallHeaderForExcel(workbook, sheet,"QTY - PACK",rowHeader, 3);
            createSmallHeaderForExcel(workbook, sheet,"MIN EXP.DATE",rowHeader, 4);
            createSmallHeaderForExcel(workbook, sheet,"PRICE-"+customerOrder.getCurrencyType(),rowHeader, 5);
            createSmallHeaderForExcel(workbook, sheet,"MANUFACTURER-ORIGIN",rowHeader, 6);
            createSmallHeaderForExcel(workbook, sheet,"TOTAL PRICE-"+customerOrder.getCurrencyType(),rowHeader, 7);
            List<CustomerOrderDrugs> drugs = customerOrder.getCustomerOrderDrugs();
            row++;
            int count=0;

            //[$$-en-US]#.##0,00 dolar
            //#.##0,00 [$€-de-DE]  euro
            //[$£-cy-GB]#.##0,00   sterlin
            //#.##0,00 ₺ tl
            String currencyLogo ="";

            String logo = "";
            if(customerOrder.getCurrencyType() == CurrencyType.USD)         logo = "$";
            if(customerOrder.getCurrencyType() == CurrencyType.EURO)        logo = "€";
            if(customerOrder.getCurrencyType()== CurrencyType.STERLIN)      logo = "£";
            if(customerOrder.getCurrencyType() == CurrencyType.TL)          logo = "₺";

            Double totalOrderPrice = 0.0;
            for (CustomerOrderDrugs drug :drugs) {

                count++;
                sheet.addMergedRegion(new CellRangeAddress(row,row,1,2));
                XSSFRow newRow = sheet.createRow((short) row );
                newRow.setHeight((short)600);


                newRow.createCell(0).setCellValue(count);
                newRow.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.CENTER, VerticalAlignment.CENTER, false));

                newRow.createCell(1).setCellValue(drug.getDrugCard().getDrugName());
                newRow.getCell(1).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));

                newRow.createCell(3).setCellValue(drug.getTotalQuantity());
                newRow.getCell(3).setCellStyle(setRowProperties(workbook,HorizontalAlignment.CENTER, VerticalAlignment.CENTER, false));

                newRow.createCell(4).setCellValue(dateFormat.format(drug.getExpirationDate()));
                newRow.getCell(4).setCellStyle(setRowProperties(workbook,HorizontalAlignment.CENTER, VerticalAlignment.CENTER, false));


                if(drug.getUnitPrice() != null && (drug.getCurrencyFee() != null) ){
                    Double currencyFee = 1.0;
                    if( drug.getCurrencyFee() != 0)
                        currencyFee = drug.getCurrencyFee();
                    Double calculate = (drug.getUnitPrice()/currencyFee);
                    Double unitPrice = ( (double) ( (int) (calculate * 100.0) ) ) / 100.0 ;

                    newRow.createCell(5).setCellValue(logo+""+unitPrice.toString());
                    newRow.getCell(5).setCellStyle(createStyleWithCurrencyLogo(workbook, customerOrder.getCurrencyType()));
                }
                else {
                    newRow.createCell(5).setCellValue(logo+"");
                    newRow.getCell(5).setCellStyle(createStyleWithCurrencyLogo(workbook, customerOrder.getCurrencyType()));
                }

                newRow.createCell(6).setCellValue(drug.getDrugCard().getDrugCompany()+" - "+drug.getEnglishCountryName());
                newRow.getCell(6).setCellStyle(setRowProperties(workbook,HorizontalAlignment.CENTER, VerticalAlignment.CENTER, false));

                if(drug.getTotalQuantity() != null && drug.getUnitPrice() != null && drug.getCurrencyFee() != null){
                    Double currencyFee = 1.0;
                    if( drug.getCurrencyFee() != 0)
                        currencyFee = drug.getCurrencyFee();
                    Double calculate2 = ((drug.getTotalQuantity()*drug.getUnitPrice())/currencyFee);
                    Double totalPrice = ( (double) ( (int) (calculate2 * 100.0) ) ) / 100.0 ;
                    totalOrderPrice += totalPrice;
                    newRow.createCell(7).setCellValue(logo+""+totalPrice.toString());
                    newRow.getCell(7).setCellStyle(createStyleWithCurrencyLogo(workbook, customerOrder.getCurrencyType(),BorderTypeEnum.RIGHT));
                }
                else{
                    newRow.createCell(7).setCellValue(logo+"");
                    newRow.getCell(7).setCellStyle(createStyleWithCurrencyLogo(workbook, customerOrder.getCurrencyType(),BorderTypeEnum.RIGHT));
                }
                //newRow.createCell(7).setCellFormula("D"+(row+1)+"*F"+(row+1));

                row++;
            }

            XSSFRow nullHeaderRow = sheet.createRow(row);
            sheet.addMergedRegion(new CellRangeAddress(row,row,0,7));
            createSmallHeaderForExcel(workbook, sheet,"",nullHeaderRow, 0);
            row++;

            XSSFRow rowCustomCost = sheet.createRow((short) row);
            rowCustomCost.createCell(6).setCellValue("CUSTOMS COST - "+customerOrder.getCurrencyType());
            rowCustomCost.getCell(6).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));
            if(totalOrderPrice != null){
                Double top = ( (double) ( (int) (totalOrderPrice * 1000.0) ) ) / 1000.0 ;
                rowCustomCost.createCell(7).setCellValue(logo+""+top);
            }
            else
                rowCustomCost.createCell(7).setCellValue(logo+"");
            rowCustomCost.getCell(7).setCellStyle(createStyleWithCurrencyLogo(workbook, customerOrder.getCurrencyType(),BorderTypeEnum.RIGHT));
            row++;

            XSSFRow rowFreightCost = sheet.createRow((short) row);
            rowFreightCost.createCell(6).setCellValue("FREIGHT COST - " +customerOrder.getCurrencyType());
            rowFreightCost.getCell(6).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));


            if(customerOrder.getFreightCostTl() != null) {
                rowFreightCost.createCell(7).setCellValue( logo + customerOrder.getFreightCostTl());
                rowFreightCost.getCell(7).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

            }else{
                rowFreightCost.createCell(7).setCellValue( logo +  " 0 ");
                rowFreightCost.getCell(7).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));

            }
            rowFreightCost.getCell(7).setCellStyle(createStyleWithCurrencyLogo(workbook, customerOrder.getCurrencyType(),BorderTypeEnum.RIGHT));
            row++;

            XSSFRow rowGrandTotal = sheet.createRow((short) row);
            rowGrandTotal.createCell(6).setCellValue("TOTAL - "+customerOrder.getCurrencyType());
            rowGrandTotal.getCell(6).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));


            if(customerOrder.getFreightCostTl() != null){
                Double top = ( (double) ( (int) (totalOrderPrice * 1000.0) ) ) / 1000.0 ;
                Double bot = ( (double) ( (int) (customerOrder.getFreightCostTl() * 1000.0) ) ) / 1000.0 ;
                rowGrandTotal.createCell(7).setCellValue(logo+""+(top+bot));
                rowGrandTotal.getCell(7).setCellStyle(createStyleWithCurrencyLogo(workbook, customerOrder.getCurrencyType(),BorderTypeEnum.RIGHT));
            }
            else
                rowGrandTotal.createCell(7).setCellValue(logo+""+totalOrderPrice);
            rowGrandTotal.getCell(7).setCellStyle(createStyleWithCurrencyLogo(workbook, customerOrder.getCurrencyType(),BorderTypeEnum.RIGHT));
            row++;


            nullHeaderRow = sheet.createRow(row);
            sheet.addMergedRegion(new CellRangeAddress(row,row,0,7));
            if(user.getEmail()!=null)
                createSmallHeaderForExcel(workbook, sheet,"www.ekippharma.com  -  "+user.getEmail(),nullHeaderRow, 0);
            else
                createSmallHeaderForExcel(workbook, sheet,"www.ekippharma.com  -  ", nullHeaderRow, 0);
            row++;

            //row5
            XSSFRow rowPaymentTerms = sheet.createRow((short) row);
            rowPaymentTerms.createCell(0).setCellValue("Payment Terms : ");
            rowPaymentTerms.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,3));

            rowPaymentTerms.createCell(4).setCellValue("BANK ACCOUNT DETAILS: ");
            rowPaymentTerms.getCell(4).setCellStyle(styleBorderLeft);
            sheet.addMergedRegion(new CellRangeAddress(row, row,4,7));

            rowPaymentTerms.createCell(7).setCellValue("");
            rowPaymentTerms.getCell(7).setCellStyle(styleBorderRight);
            row++;


            XSSFRow rowDeliveryTerms = sheet.createRow((short) row);
            rowDeliveryTerms.createCell(0).setCellValue("Delivery Terms : ");
            rowDeliveryTerms.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,3));

            rowDeliveryTerms.createCell(4).setCellValue("BANK NAME: ");
            rowDeliveryTerms.getCell(4).setCellStyle(styleBorderLeft);
            sheet.addMergedRegion(new CellRangeAddress(row, row,4,7));

            if(customerOrder.getCustomerOrderBankDetail() != null) {
                rowDeliveryTerms.createCell(6).setCellValue(customerOrder.getCustomerOrderBankDetail().getBankName());
                rowDeliveryTerms.getCell(6).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));
            }

            rowDeliveryTerms.createCell(7).setCellValue("");
            rowDeliveryTerms.getCell(7).setCellStyle(styleBorderRight);
            row++;

            XSSFRow rowLeadTime = sheet.createRow((short) row);
            rowLeadTime.createCell(0).setCellValue("Lead Time : ");
            rowLeadTime.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,3));

            rowLeadTime.createCell(4).setCellValue("BRANCH CODE ");
            rowLeadTime.getCell(4).setCellStyle(styleBorderLeft);
            sheet.addMergedRegion(new CellRangeAddress(row, row,4,7));

            if(customerOrder.getCustomerOrderBankDetail() != null){
                rowLeadTime.createCell(6).setCellValue("");
                rowLeadTime.getCell(6).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));
            }

            rowLeadTime.createCell(7).setCellValue("");
            rowLeadTime.getCell(7).setCellStyle(styleBorderRight);
            row++;

            XSSFRow rowBlaBla = sheet.createRow((short) row);
            rowBlaBla.createCell(0).setCellValue("Packaging:Unless the buyer states otherwise,products will be packaged according to GDP ");
            rowBlaBla.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,3));

            rowBlaBla.createCell(4).setCellValue("SWIFT CODE: ");
            rowBlaBla.getCell(4).setCellStyle(styleBorderLeft);
            sheet.addMergedRegion(new CellRangeAddress(row, row,4,7));

            if(customerOrder.getCustomerOrderBankDetail() != null){
                rowBlaBla.createCell(6).setCellValue(customerOrder.getCustomerOrderBankDetail().getSwift());
                rowBlaBla.getCell(6).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));
            }

            rowBlaBla.createCell(7).setCellValue("");
            rowBlaBla.getCell(7).setCellStyle(styleBorderRight);
            row++;



            XSSFRow rowPackages = sheet.createRow((short) row);
            rowPackages.createCell(0).setCellValue("Packages and Leaflets are in Turkish ");
            rowPackages.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,3));

            rowPackages.createCell(4).setCellValue("BANK ACCOUNT: ");
            rowPackages.getCell(4).setCellStyle(styleBorderLeft);
            sheet.addMergedRegion(new CellRangeAddress(row, row,4,7));

            if(customerOrder.getCustomerOrderBankDetail() != null) {
                rowPackages.createCell(6).setCellValue(customerOrder.getCustomerOrderBankDetail().getAccountName());
            rowPackages.getCell(6).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));
            }

            rowPackages.createCell(7).setCellValue("");
            rowPackages.getCell(7).setCellStyle(styleBorderRight);
            row++;

            XSSFRow rowOrdersUnder = sheet.createRow((short) row);
            rowOrdersUnder.createCell(0).setCellValue("Orders under 7.500 USD will be charged 150 USD for customs fees ");
            rowOrdersUnder.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,3));

            rowOrdersUnder.createCell(4).setCellValue("IBAN NR:");
            rowOrdersUnder.getCell(4).setCellStyle(styleBorderLeft);
            sheet.addMergedRegion(new CellRangeAddress(row, row,4,7));

            if(customerOrder.getCustomerOrderBankDetail() != null){
                rowOrdersUnder.createCell(6).setCellValue(customerOrder.getCustomerOrderBankDetail().getIbanNo());
                rowOrdersUnder.getCell(6).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));
            }

            rowOrdersUnder.createCell(7).setCellValue("");
            rowOrdersUnder.getCell(7).setCellStyle(styleBorderRight);
            row++;

            XSSFRow rowAdditionalDetails = sheet.createRow((short) row);
            rowAdditionalDetails.createCell(0).setCellValue("Additional Details: ");
            rowAdditionalDetails.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,3));

            rowAdditionalDetails.createCell(4).setCellValue("");
            rowAdditionalDetails.getCell(4).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,4,7));

            rowAdditionalDetails.createCell(4).setCellValue("");
            rowAdditionalDetails.getCell(4).setCellStyle(styleBorderLeft);

            rowAdditionalDetails.createCell(7).setCellValue("");
            rowAdditionalDetails.getCell(7).setCellStyle(styleBorderRight);
            row++;

            nullHeaderRow = sheet.createRow(row);
            sheet.addMergedRegion(new CellRangeAddress(row,row,0,7));
            createSmallHeaderForExcel(workbook, sheet,"",nullHeaderRow, 0);
            row++;

            XSSFRow rowSeller = sheet.createRow((short) row);
            rowSeller.createCell(0).setCellValue("SELLER:");
            rowSeller.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,2));

            rowSeller.createCell(4).setCellValue("BUYER:");
            rowSeller.getCell(4).setCellStyle(styleBorderLeft);
            sheet.addMergedRegion(new CellRangeAddress(row, row,4,6));

            rowSeller.createCell(7).setCellValue("");
            rowSeller.getCell(7).setCellStyle(styleBorderRight);

            row++;

            XSSFRow rowEkipAddress = sheet.createRow((short) row);
            rowEkipAddress.createCell(0).setCellValue("Ekip Ecza Deposu San ve Tic ltd Sti");
            rowEkipAddress.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,2));

            rowEkipAddress.createCell(3).setCellValue("");
            rowEkipAddress.getCell(3).setCellStyle(styleBorderRight);


            if(customerOrder.getCustomer().getCompany() != null) {
                rowEkipAddress.createCell(4).setCellValue(customerOrder.getCustomer().getCompany().getCompanyName());
                rowEkipAddress.getCell(4).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
                sheet.addMergedRegion(new CellRangeAddress(row, row, 4, 6));
            }else{
                rowEkipAddress.createCell(4).setCellValue(customerOrder.getCustomer().getName()+" "+customerOrder.getCustomer().getSurname());
                rowEkipAddress.getCell(4).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
                sheet.addMergedRegion(new CellRangeAddress(row, row, 4, 6));
            }

            rowEkipAddress.createCell(7).setCellValue("");
            rowEkipAddress.getCell(7).setCellStyle(styleBorderRight);
            row++;

            XSSFRow rowEkipAddress2 = sheet.createRow((short) row);
            rowEkipAddress2.createCell(0).setCellValue("Ostim Mahallesi 1148 Sokak No:32/C ");
            rowEkipAddress2.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,2));

            rowEkipAddress2.createCell(3).setCellValue("");
            rowEkipAddress2.getCell(3).setCellStyle(styleBorderRight);

            if(customerOrder.getCustomer().getCompany() != null) {
                rowEkipAddress2.createCell(4).setCellValue(customerOrder.getCompany().getAddress() + " " + customerOrder.getCompany().getCity());
                rowEkipAddress2.getCell(4).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
                sheet.addMergedRegion(new CellRangeAddress(row, row, 4, 6));
            }else{
                rowEkipAddress2.createCell(4).setCellValue(customerOrder.getCustomer().getOpenAddress());
                rowEkipAddress2.getCell(4).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
                sheet.addMergedRegion(new CellRangeAddress(row, row,4, 6));
            }

            rowEkipAddress2.createCell(7).setCellValue("");
            rowEkipAddress2.getCell(7).setCellStyle(styleBorderRight);
            row++;

            XSSFRow rowIlIlce = sheet.createRow((short) row);
            rowIlIlce.createCell(0).setCellValue("Yenimahalle/Ankara ");
            rowIlIlce.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,2));

            rowIlIlce.createCell(3).setCellValue("");
            rowIlIlce.getCell(3).setCellStyle(styleBorderRight);

            if(customerOrder.getCompany() !=null) {
                rowIlIlce.createCell(4).setCellValue(customerOrder.getCompany().getCountry().getEnglishName());
                rowIlIlce.getCell(4).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
                sheet.addMergedRegion(new CellRangeAddress(row, row, 4, 6));

            }else{ rowIlIlce.createCell(4).setCellValue(customerOrder.getCustomer().getCity()+ " / " + customerOrder.getCustomer().getCountry().getEnglishName());
                rowIlIlce.getCell(4).setCellStyle(setRowProperties(workbook, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
                sheet.addMergedRegion(new CellRangeAddress(row, row, 4, 6));
            }
            rowIlIlce.createCell(7).setCellValue("");
            rowIlIlce.getCell(7).setCellStyle(styleBorderRight);
            row++;


            XSSFRow rowEkipAddressUlke = sheet.createRow((short) row);
            rowEkipAddressUlke.createCell(0).setCellValue("TURKIYE");
            rowEkipAddressUlke.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,2));

            rowEkipAddressUlke.createCell(3).setCellValue("");
            rowEkipAddressUlke.getCell(3).setCellStyle(styleBorderRight);

            rowEkipAddressUlke.createCell(7).setCellValue("");
            rowEkipAddressUlke.getCell(7).setCellStyle(styleBorderRight);
            row++;


            XSSFRow rowBlank2 = sheet.createRow((short) row);
            rowBlank2.createCell(0).setCellValue("");
            rowBlank2.getCell(0).setCellStyle(styleBorderBottom);
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,1));

            rowBlank2.createCell(1).setCellValue("");
            rowBlank2.getCell(1).setCellStyle(styleBorderBottom);

            rowBlank2.createCell(2).setCellValue("");
            rowBlank2.getCell(2).setCellStyle(styleBorderBottom);

            rowBlank2.createCell(3).setCellValue("");
            rowBlank2.getCell(3).setCellStyle(styleBorderBottom);

            rowBlank2.createCell(4).setCellValue("");
            showRowWithBorder(workbook,sheet,"", rowBlank2, 4, BorderTypeEnum.LEFT, BorderTypeEnum.BOTTOM);

            rowBlank2.createCell(5).setCellValue("");
            rowBlank2.getCell(5).setCellStyle(styleBorderBottom);

            rowBlank2.createCell(6).setCellValue("");
            rowBlank2.getCell(6).setCellStyle(styleBorderBottom);

            rowBlank2.createCell(7).setCellValue("");
            showRowWithBorder(workbook,sheet,"", rowBlank2, 7, BorderTypeEnum.RIGHT, BorderTypeEnum.BOTTOM);
            row++;

            //set signature
            createSignatureForExcel(workbook, sheet,row);

            //A4 Sayfaya Sığdırma
            sheet.setFitToPage(true);
            PrintSetup ps = sheet.getPrintSetup();
            ps.setPaperSize(PrintSetup.A4_PAPERSIZE);
            ps.setFitWidth( (short) 1);
            ps.setFitHeight( (short) 0);


            FileOutputStream fileOut = new FileOutputStream(
                    "docs/"+customerOrder.getCustomerOrderNo()+".xlsx");
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
            String fileName = customerOrder.getCustomerOrderNo();

            return fileName;
        }catch (Exception e){
            throw new Exception("Sipariş Excel Oluşturma İşleminde Hata Oluştu.",e);
        }
    }

    public String createCustomerOrderExcelV3(String authHeader, Long customerId, Long customerOrderId) throws Exception {

        try {
            Optional<Object> result = null;
            User user = this.getUserFromToken(authHeader);
            Optional<CustomerOrder> optionalCustomerOrder= null;
            Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
            if(!optionalCustomer.isPresent())
                throw new Exception("Böyle Bir Müşteri Yoktur");
            if (user.getRole() == Role.ADMIN ) {
                optionalCustomerOrder = customerOrderRepository.getSingleCustomerOrder(customerOrderId, optionalCustomer.get().getCustomerId());
                if (!optionalCustomerOrder.isPresent())
                    throw new Exception("Böyle Bir Sipariş Yoktur");
            } else if (user.getRole() == Role.EXPORTER){
                optionalCustomerOrder = customerOrderRepository.getSingleCustomerOrderForExporter(customerOrderId, optionalCustomer.get().getCustomerId(), user.getUserId());
                if (!optionalCustomerOrder.isPresent())
                    throw new Exception("Böyle Bir Sipariş Yoktur");

            }
            CustomerOrder customerOrder = optionalCustomerOrder.get();
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Proforma Invoice");

            //set header
            createHeaderForExcel(workbook, sheet, "PROFORMA INVOICE");

            //set logo
            createLogoForExcel(workbook, sheet);

            //set border
            XSSFCellStyle styleBorderLeft = setRowProperties(workbook,HorizontalAlignment.LEFT,VerticalAlignment.CENTER, true);
            styleBorderLeft.setBorderLeft(BorderStyle.MEDIUM);
            XSSFCellStyle styleBorderBottom = setRowProperties(workbook,HorizontalAlignment.CENTER,VerticalAlignment.CENTER, true);
            styleBorderBottom.setBorderBottom(BorderStyle.MEDIUM);
            XSSFCellStyle styleBorderRight = setRowProperties(workbook,HorizontalAlignment.CENTER,VerticalAlignment.CENTER, true);
            styleBorderRight.setBorderRight(BorderStyle.MEDIUM);

            //row 3
            XSSFRow row3 = sheet.createRow((short) 3);
            row3.createCell(2).setCellValue("Reference No : "+customerOrder.getCustomerOrderNo());
            row3.getCell(2).setCellStyle(setRowProperties(workbook,HorizontalAlignment.CENTER, VerticalAlignment.CENTER, true));

            //row4
            XSSFRow row4 = sheet.createRow((short) 4);
            row4.createCell(2).setCellValue("Date : "+customerOrder.getOrderDate());
            row4.getCell(2).setCellStyle(setRowProperties(workbook,HorizontalAlignment.CENTER, VerticalAlignment.CENTER, true));

            //row2
            CellRangeAddress mergedCell = new CellRangeAddress(2,2,5,7);
            sheet.addMergedRegion(mergedCell);
            XSSFRow row2 = sheet.createRow((short) 2);
            showRowWithBorder(workbook,"Invoice Address : ", row2, 5, HorizontalAlignment.LEFT,VerticalAlignment.CENTER, true, BorderTypeEnum.TOP, BorderTypeEnum.LEFT);
//            XSSFCellStyle style = setRowProperties(workbook,HorizontalAlignment.LEFT,VerticalAlignment.CENTER, true);
//            row2.getCell(5).setCellStyle(style);
            showRowWithBorder(workbook,sheet,"", row2, 6, BorderTypeEnum.TOP);
            showRowWithBorder(workbook,sheet,"", row2, 7, BorderTypeEnum.RIGHT, BorderTypeEnum.TOP);

            sheet.setColumnWidth(0, 1500);
            sheet.setColumnWidth(1, 5500);
            sheet.setColumnWidth(2, 9000);
            sheet.setColumnWidth(3, 2500);
            sheet.setColumnWidth(4, 3000);
            sheet.setColumnWidth(5, 4000);
            sheet.setColumnWidth(6, 8000);
            sheet.setColumnWidth(7, 4000);


            //row3
            showRowWithBorder(workbook,sheet,"", row3, 5, BorderTypeEnum.LEFT);

            row3.createCell(6).setCellValue(customerOrder.getCustomer().getName()+" "+customerOrder.getCustomer().getSurname());
            row3.getCell(6).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT,VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(3,3,6,7));

            showRowWithBorder(workbook,sheet,"", row3, 7, BorderTypeEnum.RIGHT);


            if(customerOrder.getCustomer().getCountry() != null){


                showRowWithBorder(workbook,sheet,"", row4, 5, BorderTypeEnum.LEFT, BorderTypeEnum.BOTTOM);

                row4.createCell(6).setCellValue(customerOrder.getCustomer().getCountry().getName());
                row4.getCell(6).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT,VerticalAlignment.CENTER, true));
                row4.getCell(6).setCellStyle(styleBorderBottom);

                showRowWithBorder(workbook,sheet,"", row4, 7, BorderTypeEnum.RIGHT, BorderTypeEnum.BOTTOM);

            } else{
                showRowWithBorder(workbook,sheet,"", row4, 5, BorderTypeEnum.LEFT, BorderTypeEnum.BOTTOM);
                row4.createCell(6).setCellValue("");
                row4.getCell(6).setCellStyle(styleBorderBottom);
                showRowWithBorder(workbook,sheet,"", row4, 7, BorderTypeEnum.RIGHT, BorderTypeEnum.BOTTOM);
            }


            int row = 9;
            XSSFRow rowHeader = sheet.createRow(row);
            createSmallHeaderForExcel(workbook, sheet,"",rowHeader, 0);
            sheet.addMergedRegion(new CellRangeAddress(row,row,1,2));
            createSmallHeaderForExcel(workbook, sheet,"PRODUCTS",rowHeader, 1);
            //createSmallHeaderForExcel(workbook, sheet,"PRODUCTS",rowHeader, 2);
            createSmallHeaderForExcel(workbook, sheet,"QTY",rowHeader, 3);
            createSmallHeaderForExcel(workbook, sheet,"EXP.DATE",rowHeader, 4);
            createSmallHeaderForExcel(workbook, sheet,"PRICE-"+customerOrder.getCurrencyType(),rowHeader, 5);
            createSmallHeaderForExcel(workbook, sheet,"MANUFACTURE-ORIGIN",rowHeader, 6);
            createSmallHeaderForExcel(workbook, sheet,"TOTAL PRICE-"+customerOrder.getCurrencyType(),rowHeader, 7);
            List<CustomerOrderDrugs> drugs = customerOrder.getCustomerOrderDrugs();
            row=10;
            int count=0;

            //[$$-en-US]#.##0,00 dolar
            //#.##0,00 [$€-de-DE]  euro
            //[$£-cy-GB]#.##0,00   sterlin
            //#.##0,00 ₺ tl
            String currencyLogo ="";

            for (CustomerOrderDrugs drug :drugs) {

                count++;
                sheet.addMergedRegion(new CellRangeAddress(row,row,1,2));
                XSSFRow newRow = sheet.createRow((short) row );


                newRow.createCell(0).setCellValue(count);
                newRow.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.CENTER, VerticalAlignment.CENTER, false));

                newRow.createCell(1).setCellValue(drug.getDrugCard().getDrugName());
                newRow.getCell(1).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));

                newRow.createCell(3).setCellValue(drug.getTotalQuantity());
                newRow.getCell(3).setCellStyle(setRowProperties(workbook,HorizontalAlignment.CENTER, VerticalAlignment.CENTER, false));

                newRow.createCell(4).setCellValue(dateFormat.format(drug.getExpirationDate()));
                newRow.getCell(4).setCellStyle(setRowProperties(workbook,HorizontalAlignment.CENTER, VerticalAlignment.CENTER, false));

                newRow.createCell(5).setCellValue(" ");
                newRow.getCell(5).setCellStyle(createStyleWithCurrencyLogo(workbook, customerOrder.getCurrencyType()));

                newRow.createCell(6).setCellValue(drug.getDrugCard().getDrugCompany()+" - "+drug.getDrugCard().getSourceCountry());
                newRow.getCell(6).setCellStyle(setRowProperties(workbook,HorizontalAlignment.CENTER, VerticalAlignment.CENTER, false));

                newRow.createCell(7).setCellFormula("D"+(row+1)+"*F"+(row+1));
                newRow.getCell(7).setCellStyle(createStyleWithCurrencyLogo(workbook, customerOrder.getCurrencyType(),BorderTypeEnum.RIGHT));
                row++;
            }

            XSSFRow nullHeaderRow = sheet.createRow(row);
            sheet.addMergedRegion(new CellRangeAddress(row,row,0,7));
            createSmallHeaderForExcel(workbook, sheet,"",nullHeaderRow, 0);
            row++;

            XSSFRow row18 = sheet.createRow((short) row);
            row18.createCell(6).setCellValue("CUSTOMS COST - "+customerOrder.getCurrencyType());
            row18.getCell(6).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));
            row18.createCell(7).setCellValue(" ");
            row18.getCell(7).setCellStyle(createStyleWithCurrencyLogo(workbook, customerOrder.getCurrencyType(),BorderTypeEnum.RIGHT));
            row++;

            XSSFRow row19 = sheet.createRow((short) row);
            row19.createCell(6).setCellValue("FREIGHT COST - "+customerOrder.getCurrencyType());
            row19.getCell(6).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));
            row19.createCell(7).setCellValue(" ");
            row19.getCell(7).setCellStyle(createStyleWithCurrencyLogo(workbook, customerOrder.getCurrencyType(),BorderTypeEnum.RIGHT));
            row++;

            XSSFRow row20 = sheet.createRow((short) row);
            row20.createCell(6).setCellValue("GRAND TOTAL - "+customerOrder.getCurrencyType());
            row20.getCell(6).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false));
            row20.createCell(7).setCellValue(currencyLogo+" ");
            row20.getCell(7).setCellStyle(createStyleWithCurrencyLogo(workbook, customerOrder.getCurrencyType(),BorderTypeEnum.RIGHT));
            row++;

            nullHeaderRow = sheet.createRow(row);
            sheet.addMergedRegion(new CellRangeAddress(row,row,0,7));
            if(user.getEmail()!=null)
                createSmallHeaderForExcel(workbook, sheet,"www.ekippharma.com  -  "+user.getEmail(),nullHeaderRow, 0);
            else
                createSmallHeaderForExcel(workbook, sheet,"www.ekippharma.com  -  ", nullHeaderRow, 0);
            row++;

            //row5
            XSSFRow row5 = sheet.createRow((short) row);
            row5.createCell(0).setCellValue("Payment Terms : ");
            row5.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,3));

            row5.createCell(4).setCellValue("BANK ACCOUNT DETAILS: ");
            row5.getCell(4).setCellStyle(styleBorderLeft);
            sheet.addMergedRegion(new CellRangeAddress(row, row,4,5));

            row5.createCell(7).setCellValue("");
            row5.getCell(7).setCellStyle(styleBorderRight);
            row++;


            XSSFRow row6 = sheet.createRow((short) row);
            row6.createCell(0).setCellValue("Delivery Terms : ");
            row6.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,3));

            row6.createCell(4).setCellValue("");
            row6.getCell(4).setCellStyle(styleBorderLeft);

            row6.createCell(7).setCellValue("");
            row6.getCell(7).setCellStyle(styleBorderRight);
            row++;

            XSSFRow row7 = sheet.createRow((short) row);
            row7.createCell(0).setCellValue("Lead Time : ");
            row7.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,3));

            row7.createCell(4).setCellValue("BANK NAME: ");
            row7.getCell(4).setCellStyle(styleBorderLeft);
            sheet.addMergedRegion(new CellRangeAddress(row, row,4,5));

            row7.createCell(7).setCellValue("");
            row7.getCell(7).setCellStyle(styleBorderRight);
            row++;

            XSSFRow row8 = sheet.createRow((short) row);
            row8.createCell(0).setCellValue("Packaging:Unless the buyer states otherwise,products will be packaged according to GDP ");
            row8.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.CENTER, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,3));

            row8.createCell(4).setCellValue("BANK A/C NAME: ");
            row8.getCell(4).setCellStyle(styleBorderLeft);
            sheet.addMergedRegion(new CellRangeAddress(row, row,4,5));

            row8.createCell(7).setCellValue("");
            row8.getCell(7).setCellStyle(styleBorderRight);
            row++;

            XSSFRow row9 = sheet.createRow((short) row);
            row9.createCell(0).setCellValue("Packages and Leaflets are in Turkish ");
            row9.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,3));

            row9.createCell(4).setCellValue("SWIFT CODE: ");
            row9.getCell(4).setCellStyle(styleBorderLeft);
            sheet.addMergedRegion(new CellRangeAddress(row, row,4,5));

            row9.createCell(7).setCellValue("");
            row9.getCell(7).setCellStyle(styleBorderRight);
            row++;

            XSSFRow row10 = sheet.createRow((short) row);
            row10.createCell(0).setCellValue("Orders under 7.500 USD will be charged 150 USD for customs fees ");
            row10.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,3));

            row10.createCell(4).setCellValue("BANK ACCOUNT: ");
            row10.getCell(4).setCellStyle(styleBorderLeft);
            sheet.addMergedRegion(new CellRangeAddress(row, row,4,5));

            row10.createCell(7).setCellValue("");
            row10.getCell(7).setCellStyle(styleBorderRight);
            row++;

            XSSFRow row11 = sheet.createRow((short) row);
            row11.createCell(0).setCellValue("Additional Details: ");
            row11.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,3));

            row11.createCell(4).setCellValue("IBAN NR: ");
            row11.getCell(4).setCellStyle(styleBorderLeft);
            sheet.addMergedRegion(new CellRangeAddress(row, row,4,5));

            row11.createCell(7).setCellValue("");
            row11.getCell(7).setCellStyle(styleBorderRight);
            row++;

            XSSFRow row12 = sheet.createRow((short) row);
            row12.createCell(0).setCellValue("");
            row12.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,3));

            row12.createCell(4).setCellValue("");
            row12.getCell(4).setCellStyle(styleBorderLeft);
            sheet.addMergedRegion(new CellRangeAddress(row, row,4,5));

            row12.createCell(7).setCellValue("");
            row12.getCell(7).setCellStyle(styleBorderRight);
            row++;

            nullHeaderRow = sheet.createRow(row);
            sheet.addMergedRegion(new CellRangeAddress(row,row,0,7));
            createSmallHeaderForExcel(workbook, sheet,"",nullHeaderRow, 0);
            row++;

            XSSFRow row13 = sheet.createRow((short) row);
            row13.createCell(0).setCellValue("SELLER:  Ekip Ecza Deposu San ve Tic ltd Sti");
            row13.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,2));

            row13.createCell(4).setCellValue("BUYER: ");
            row13.getCell(4).setCellStyle(styleBorderLeft);
            sheet.addMergedRegion(new CellRangeAddress(row, row,4,6));

            row13.createCell(7).setCellValue("");
            row13.getCell(7).setCellStyle(styleBorderRight);
            row++;


            //set signature
            createSignatureForExcel(workbook, sheet,row);

            XSSFRow row14 = sheet.createRow((short) row);
            row14.createCell(0).setCellValue("Ostim mahallesi 1148 Sokak No:32/C ");
            row14.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,1));

            row14.createCell(4).setCellValue("");
            row14.getCell(4).setCellStyle(styleBorderLeft);

            row14.createCell(7).setCellValue("");
            row14.getCell(7).setCellStyle(styleBorderRight);
            row++;

            XSSFRow row15 = sheet.createRow((short) row);
            row15.createCell(0).setCellValue("Yenimahalle/Ankara ");
            row15.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,1));

            row15.createCell(4).setCellValue("");
            row15.getCell(4).setCellStyle(styleBorderLeft);

            row15.createCell(5).setCellValue(customerOrder.getCustomer().getName()+" "+customerOrder.getCustomer().getSurname());
            row15.getCell(5).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,5,6));

            row15.createCell(7).setCellValue("");
            row15.getCell(7).setCellStyle(styleBorderRight);
            row++;


            XSSFRow row16 = sheet.createRow((short) row);
            showRowWithBorder(workbook,sheet,"TURKEY ", row16, 0, BorderTypeEnum.LEFT);
            row16.getCell(0).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT, VerticalAlignment.CENTER, true));
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,1));

            showRowWithBorder(workbook,sheet,"", row16, 4, BorderTypeEnum.LEFT);

            if(customerOrder.getCustomer().getCountry() != null){
                row16.createCell(5).setCellValue(customerOrder.getCustomer().getCountry().getName());
                row16.getCell(5).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT,VerticalAlignment.CENTER, true));
                showRowWithBorder(workbook,sheet,"", row16, 7, BorderTypeEnum.RIGHT);
            }else{
                row16.createCell(5).setCellValue("");
                row16.getCell(5).setCellStyle(setRowProperties(workbook,HorizontalAlignment.LEFT,VerticalAlignment.CENTER, true));
                showRowWithBorder(workbook,sheet,"", row16, 7, BorderTypeEnum.RIGHT);
            }
            row++;


            XSSFRow row17 = sheet.createRow((short) row);
            row17.createCell(0).setCellValue("");
            row17.getCell(0).setCellStyle(styleBorderBottom);
            sheet.addMergedRegion(new CellRangeAddress(row, row,0,1));

            row17.createCell(1).setCellValue("");
            row17.getCell(1).setCellStyle(styleBorderBottom);

            row17.createCell(2).setCellValue("");
            row17.getCell(2).setCellStyle(styleBorderBottom);

            row17.createCell(3).setCellValue("");
            row17.getCell(3).setCellStyle(styleBorderBottom);

            row17.createCell(4).setCellValue("");
            showRowWithBorder(workbook,sheet,"", row17, 4, BorderTypeEnum.LEFT, BorderTypeEnum.BOTTOM);

            row17.createCell(5).setCellValue("");
            row17.getCell(5).setCellStyle(styleBorderBottom);

            row17.createCell(6).setCellValue("");
            row17.getCell(6).setCellStyle(styleBorderBottom);

            row17.createCell(7).setCellValue("");
            showRowWithBorder(workbook,sheet,"", row17, 7, BorderTypeEnum.RIGHT, BorderTypeEnum.BOTTOM);
            row++;



            //A4 Sayfaya Sığdırma
            sheet.setFitToPage(true);
            PrintSetup ps = sheet.getPrintSetup();
            ps.setPaperSize(PrintSetup.A4_PAPERSIZE);
            ps.setFitWidth( (short) 1);
            ps.setFitHeight( (short) 0);


            FileOutputStream fileOut = new FileOutputStream(
                    "docs/"+customerOrder.getCustomerOrderNo()+"-"+user.getUserId()+".xlsx");
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
            String fileName = customerOrder.getCustomerOrderNo()+"-"+user.getUserId();

            return fileName;
        }catch (Exception e){
            throw new Exception("Sipariş Excel Oluşturma İşleminde Hata Oluştu.",e);
        }
    }

    private void showRowWithBorder(XSSFWorkbook workbook,XSSFSheet sheet,String text,XSSFRow xssfRow ,
                                   int col,BorderTypeEnum ...borderList){
        //  XSSFRow xssfRow = sheet.createRow((short) row);
        xssfRow.createCell(col).setCellValue(text);
        XSSFCellStyle style = setRowProperties(workbook,HorizontalAlignment.CENTER,VerticalAlignment.CENTER, false);
        for (BorderTypeEnum borderType: borderList
        ) {
            if(borderType == BorderTypeEnum.TOP)
                style.setBorderTop(BorderStyle.MEDIUM);
            if(borderType == BorderTypeEnum.BOTTOM)
                style.setBorderBottom(BorderStyle.MEDIUM);
            if(borderType == BorderTypeEnum.RIGHT)
                style.setBorderRight(BorderStyle.MEDIUM);
            if(borderType == BorderTypeEnum.LEFT)
                style.setBorderLeft(BorderStyle.MEDIUM);
        }
        xssfRow.getCell(col).setCellStyle(style);
    }

    private void showRowWithBorder(XSSFWorkbook workbook,String text,XSSFRow xssfRow ,
                                   int col,HorizontalAlignment ha, VerticalAlignment va, Boolean isBold, BorderTypeEnum ...borderList){
        //  XSSFRow xssfRow = sheet.createRow((short) row);
        xssfRow.createCell(col).setCellValue(text);
        XSSFCellStyle style = setRowProperties(workbook, ha, va, isBold);
        for (BorderTypeEnum borderType: borderList ) {
            if(borderType == BorderTypeEnum.TOP)
                style.setBorderTop(BorderStyle.MEDIUM);
            if(borderType == BorderTypeEnum.BOTTOM)
                style.setBorderBottom(BorderStyle.MEDIUM);
            if(borderType == BorderTypeEnum.RIGHT)
                style.setBorderRight(BorderStyle.MEDIUM);
            if(borderType == BorderTypeEnum.LEFT)
                style.setBorderLeft(BorderStyle.MEDIUM);
        }
        xssfRow.getCell(col).setCellStyle(style);
    }

    private XSSFCellStyle setRowProperties(
            XSSFWorkbook workbook,
            HorizontalAlignment horizontalAlignment,
            VerticalAlignment verticalAlignment,
            Boolean setBold){

        XSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(horizontalAlignment);
        style.setVerticalAlignment(verticalAlignment);
        //style.setBorderTop(BorderStyle.MEDIUM);
        style.setWrapText(true);
        XSSFFont fontHeading = workbook.createFont();
        fontHeading.setFontName("Times New Roman");
        fontHeading.setBold(setBold);
        fontHeading.setFontHeightInPoints((short) 8);
        style.setFont(fontHeading);
        return style;
    }
    private void createHeaderForExcel(XSSFWorkbook workbook, XSSFSheet sheet, String header){

        XSSFCellStyle style = workbook.createCellStyle();
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFColor blue = new XSSFColor(new java.awt.Color(39, 89, 216));
        style.setFillForegroundColor(blue);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setWrapText(true);
        XSSFFont fontHeading = workbook.createFont();
        fontHeading.setFontName("Times New Roman");
        fontHeading.setBold(true);
        fontHeading.setFontHeightInPoints((short) 20);
        fontHeading.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
        style.setFont(fontHeading);

        XSSFRow rowHeader = sheet.createRow(0);
        sheet.setColumnWidth(0, 5000);
        rowHeader.createCell(0).setCellValue(header);
        rowHeader.getCell(0).setCellStyle(style);
        rowHeader.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
        sheet.addMergedRegion(new CellRangeAddress(0,0,0,7));


//        //A4 Sayfaya Sığdırma
//        sheet.setFitToPage(true);
//        PrintSetup ps = sheet.getPrintSetup();
//        ps.setPaperSize(PrintSetup.A4_PAPERSIZE);
//        ps.setFitWidth( (short) 1);
//        ps.setFitHeight( (short) 0);
    }
    private void createSmallHeaderForExcel(
            XSSFWorkbook workbook,
            XSSFSheet sheet,
            String header,
            XSSFRow rowHeader,
            int column  ){

        XSSFCellStyle style = workbook.createCellStyle();
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFColor blue = new XSSFColor(new java.awt.Color(39, 89, 216));
        style.setFillForegroundColor(blue);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setWrapText(true);
        XSSFFont fontHeading = workbook.createFont();
        fontHeading.setFontName("Times New Roman");
        fontHeading.setFontHeightInPoints((short) 8);
        fontHeading.setBold(true);
        fontHeading.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
        style.setFont(fontHeading);

        //sheet.setColumnWidth(column, 5000);
        rowHeader.createCell(column).setCellValue(header);
        rowHeader.getCell(column).setCellStyle(style);
        //rowHeader.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
    }
    private void createLogoForExcel(XSSFWorkbook workbook, XSSFSheet sheet ) throws Exception{
        InputStream inputStream = new FileInputStream("image/logo/EkipLogo.png");
        byte[] bytes = IOUtils.toByteArray(inputStream);
        int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
        inputStream.close();
        CreationHelper helper = workbook.getCreationHelper();
        Drawing drawing = sheet.createDrawingPatriarch();
        ClientAnchor anchor = helper.createClientAnchor();
        anchor.setRow1(1);
        anchor.setCol1(0);
        Picture pict = drawing.createPicture(anchor, pictureIdx);
            pict.resize(1.41, 7);

        sheet.addMergedRegion(new CellRangeAddress(1, 7, 0, 1));
    }

    private void createSignatureForExcel(XSSFWorkbook workbook, XSSFSheet sheet, int row ) throws Exception{
        InputStream inputStream = new FileInputStream("image/logo/EkipSignature.png");
        byte[] bytes = IOUtils.toByteArray(inputStream);
        int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
        inputStream.close();
        CreationHelper helper = workbook.getCreationHelper();
        Drawing drawing = sheet.createDrawingPatriarch();
        ClientAnchor anchor = helper.createClientAnchor();
        anchor.setRow1(row+1);
        anchor.setCol1(6);
        Picture pict = drawing.createPicture(anchor, pictureIdx);
        pict.resize(1.2777, 4);

        sheet.addMergedRegion(new CellRangeAddress(row, row+3, 2, 3));
    }

    private XSSFCellStyle createStyleWithCurrencyLogo (XSSFWorkbook workbook,CurrencyType currencyType, BorderTypeEnum ...borderList){

        XSSFCellStyle style = setRowProperties(workbook,HorizontalAlignment.CENTER, VerticalAlignment.CENTER, false);
        DataFormat df = workbook.createDataFormat();
        if(currencyType == CurrencyType.USD)        style.setDataFormat(df.getFormat("$#,#0.00"));
        if(currencyType == CurrencyType.EURO)       style.setDataFormat(df.getFormat("€#,#0.00"));
        if(currencyType== CurrencyType.STERLIN)     style.setDataFormat(df.getFormat("£#,#0.00"));
        if(currencyType == CurrencyType.TL)         style.setDataFormat(df.getFormat("₺#,#0.00"));

        for (BorderTypeEnum borderType: borderList ) {
            if(borderType == BorderTypeEnum.TOP)    style.setBorderTop(BorderStyle.MEDIUM);
            if(borderType == BorderTypeEnum.BOTTOM) style.setBorderBottom(BorderStyle.MEDIUM);
            if(borderType == BorderTypeEnum.RIGHT)  style.setBorderRight(BorderStyle.MEDIUM);
            if(borderType == BorderTypeEnum.LEFT)   style.setBorderLeft(BorderStyle.MEDIUM);
        }
        return style;
    }

    enum BorderTypeEnum {
        BOTTOM,
        TOP,
        LEFT,
        RIGHT

    }

    public Boolean setCustomerOrderStatusTo65(String authHeader, Long customerId, Long customerOrderId) throws Exception{
        User user = this.getUserFromToken(authHeader);
        Optional<CustomerOrder> optionalCustomerOrder= null;
        Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
        if (user.getRole() == Role.ADMIN) {
            optionalCustomerOrder = customerOrderRepository.getSingleCustomerOrder(customerOrderId, optionalCustomer.get().getCustomerId());

        }else if (user.getRole() == Role.EXPORTER){
            optionalCustomerOrder = customerOrderRepository.getSingleCustomerOrderForExporter(customerOrderId, optionalCustomer.get().getCustomerId(), user.getUserId());

        }
        if(!optionalCustomerOrder.isPresent())
            throw new Exception("Böyle Bir Sipariş Yoktur");


        List<Box> boxList = boxRepository.findBoxesForOutFromDepot(customerOrderId);
        if(boxList.size()>0){
            for (Box box:boxList) {
                box.setStatus(0);
                box = boxRepository.save(box);
            }
        }

        List<SmallBox> smallBoxList = smallBoxRepository.findSmallBoxesForOutFromDepot(customerOrderId);
        if(smallBoxList.size()>0){
            for (SmallBox smallBox:smallBoxList) {
                smallBox.setStatus(0);
                smallBox = smallBoxRepository.save(smallBox);
            }
        }

        List<Depot> depotList = depotRepository.findDrugsForOutFromDepot(customerOrderId);
        if(depotList.size()>0){
            for (Depot depot:depotList) {
                depot.setDepotStatus(depotStatusRepository.findById(90l).get());
                depot.setSendingDate(new Date());
                depot = depotRepository.save(depot);
            }
        }

        CustomerOrder customerOrder= optionalCustomerOrder.get();
        Optional<CustomerOrderStatus> customerOrderStatus = orderStatusRepository.findById(65L);
        customerOrder.setOrderStatus(customerOrderStatus.get());
        if(!customerOrderStatusHistoryService.save(customerOrder,customerOrderStatus.get()))
            throw new Exception("Sipariş Oluşturulamadı");
        customerOrderRepository.save(customerOrder);

        return true;
    }

    public Boolean setCustomerOrderStatusBackTo50(String authHeader, Long customerId, Long customerOrderId) throws Exception{
        User user = this.getUserFromToken(authHeader);
        Optional<CustomerOrder> optionalCustomerOrder= null;
        Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
        if (user.getRole() == Role.ADMIN) {
            optionalCustomerOrder = customerOrderRepository.getSingleCustomerOrder(customerOrderId, optionalCustomer.get().getCustomerId());

        }else if (user.getRole() == Role.EXPORTER){
            optionalCustomerOrder = customerOrderRepository.getSingleCustomerOrderForExporter(customerOrderId, optionalCustomer.get().getCustomerId(), user.getUserId());

        }
        if(!optionalCustomerOrder.isPresent())
            throw new Exception("Böyle Bir Sipariş Yoktur");


        List<Box> boxList = boxRepository.findBoxesForOutFromDepot(customerOrderId);
        if(boxList.size()>0){
            for (Box box:boxList) {
                box.setStatus(1);
                box = boxRepository.save(box);
            }
        }

        List<SmallBox> smallBoxList = smallBoxRepository.findSmallBoxesForOutFromDepot(customerOrderId);
        if(smallBoxList.size()>0){
            for (SmallBox smallBox:smallBoxList) {
                smallBox.setStatus(1);
                smallBox = smallBoxRepository.save(smallBox);
            }
        }

        List<Depot> depotList = depotRepository.findDrugsForOutFromDepot(customerOrderId);
        if(depotList.size()>0){
            for (Depot depot:depotList) {
                depot.setDepotStatus(depotStatusRepository.findById(1l).get());
                depot = depotRepository.save(depot);
            }
        }

        CustomerOrder customerOrder= optionalCustomerOrder.get();
        Optional<CustomerOrderStatus> customerOrderStatus = orderStatusRepository.findById(50L);
        customerOrder.setOrderStatus(customerOrderStatus.get());
        if(!customerOrderStatusHistoryService.save(customerOrder,customerOrderStatus.get()))
            throw new Exception("Sipariş Oluşturulamadı");
        customerOrderRepository.save(customerOrder);

        return true;
    }
}

