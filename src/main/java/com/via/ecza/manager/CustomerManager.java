package com.via.ecza.manager;


import com.via.ecza.dto.*;
import com.via.ecza.entity.*;
import com.via.ecza.entity.enumClass.CheckingCardType;
import com.via.ecza.entity.enumClass.Role;
import com.via.ecza.manager.service.CustomerService;
import com.via.ecza.repo.*;
import com.via.ecza.service.ControlService;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.math.BigInteger;
import java.util.*;

@Slf4j
@Service
@Transactional
public class CustomerManager  implements CustomerService {


    //@PersistenceContext // or even @Autowired
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private CustomerOrderRepository customerOrderRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private CheckingCardRepository checkingCardRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ControlService controlService;
    @Autowired
    private CountryRepository countryRepository;

    @Override
    public Boolean save(String authHeader, @Valid CustomerSaveDto dto) {

        try {
            String username = controlService.getUsernameFromToken(authHeader);
            Optional<User> optUser = userRepository.findByUsername(username);
            if (!optUser.isPresent()) {
                return false;
            }
            Optional<Country> country = countryRepository.findById(dto.getCountryId());
            if (!country.isPresent()) {
                return false;
            }
            Customer customer = mapper.map(dto, Customer.class);
            if (dto.getCompanyId() != null) {
                Optional<Company> optCompany = companyRepository.findById(dto.getCompanyId());
                if (optCompany.isPresent()) {
                    optCompany.get().getCustomers().add(customer);
                    customer.setCompany(optCompany.get());
                }
            }
            customer.setStatus(1);
            customer.setUser(optUser.get());
            customer.setCreatedDate(new Date());
            customer.setCountry(country.get());
            customer.setMobilePhoneExtra(dto.getMobilePhoneExtra());
            String name=customer.getName().trim();
            String surname=customer.getSurname().trim();
            customer.setName(name);
            customer.setSurname(surname);
            customer = customerRepository.save(customer);

            /* Müşteri Şirkete Bağlı Değilse Müşteri Cari Kartı Oluşur */
            if(customer.getCompany() == null) {
                CheckingCard checkingCard = new CheckingCard();
                checkingCard.setCheckingCardName(customer.getName() + " " + customer.getSurname());
                checkingCard.setCountry(customer.getCountry());
                checkingCard.setCity(customer.getCity());
                checkingCard.setAddress(customer.getOpenAddress());
                checkingCard.setEmail(customer.getEposta());
                checkingCard.setPhoneNumber(customer.getBusinessPhone());
                checkingCard.setFaxNumber(customer.getCustomerFax());
                checkingCard.setType(CheckingCardType.CUSTOMER);
                checkingCard.setSalesRepresentative(String.valueOf(customer.getUser().getUserId()));
                checkingCard.setCustomerId(customer.getCustomerId());
                if(customer.getCompany() != null)
                    checkingCard.setCompanyId(customer.getCompany().getCompanyId());
                checkingCard.setUser(optUser.get());
                checkingCard.setCreatedAt(new Date());
                checkingCard = checkingCardRepository.save(checkingCard);
            }
            /* Cari Kart Oluşturma Bitti */


            return true;
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public CustomerDto findById(String authHeader, Long customerId) throws NotFoundException {
        String username = controlService.getUsernameFromToken(authHeader);
        Optional<User> optUser = userRepository.findByUsername(username);
        if (!optUser.isPresent()) {
            throw new NotFoundException("Not found User");
        }
        if (optUser.get().getRole() == Role.ADMIN) {
            Optional<Customer> optCustomer = customerRepository.findById(customerId);
            if (!optCustomer.isPresent()) {
                throw new NotFoundException("Not found customer");
            }
            CustomerDto customerDto = mapper.map(optCustomer.get(), CustomerDto.class);
            if (optCustomer.get().getCompany() != null) {
                SingleCompanyDto companyDto = mapper.map(optCustomer.get().getCompany(), SingleCompanyDto.class);
                customerDto.setCompany(companyDto);
            }
            return customerDto;

        } else if (optUser.get().getRole() == Role.EXPORTER) {
            Customer customer = customerRepository.findByCustomerIdandUserid(customerId, optUser.get().getUserId());
            if (customer != null) {
                CustomerDto customerDto = mapper.map(customer, CustomerDto.class);
                if (customer.getCompany() != null) {
                    SingleCompanyDto companyDto = mapper.map(customer.getCompany(), SingleCompanyDto.class);
                    customerDto.setCompany(companyDto);
                }
                return customerDto;
            }
        }

        return null;
    }


    @Override
    public Boolean update(String authHeader, Long customerId, CustomerSaveDto dto) throws Exception {

        try {
            User user = this.getUserFromToken(authHeader);
            Optional<Customer> optCustomer = customerRepository.findById(customerId);
            if (!optCustomer.isPresent()) {
                throw new NotFoundException("Not found customer");
            }
            Customer customer = optCustomer.get();

            /* Cari Kart Kullanıyor */
            String checkingCardName=customer.getName()+" "+customer.getSurname();

            Optional<Country> country = countryRepository.findById(dto.getCountryId());
            if (!country.isPresent()) {
                return false;
            }
            customer.setCountry(country.get());
            if (dto.getCompanyId() != null) {
                Optional<Company> optCompany = companyRepository.findById(dto.getCompanyId());
                if (optCompany.isPresent()) {

                    customer.setCompany(optCompany.get());
                    if(customer.getCustomerOrders() != null){
                        if(customer.getCustomerOrders().size() > 0){
                            for (CustomerOrder co : customer.getCustomerOrders()){
                                co.setCompany(customer.getCompany());
                                co = customerOrderRepository.save(co);
                            }
                        }
                    }
                } else {
                    customer.setCompany(null);
                    if(customer.getCustomerOrders() != null){
                        if(customer.getCustomerOrders().size() > 0){
                            for (CustomerOrder co : customer.getCustomerOrders()){
                                co.setCompany(null);
                                co = customerOrderRepository.save(co);
                            }
                        }
                    }
                }
            } else {
                customer.setCompany(null);
                if(customer.getCustomerOrders() != null){
                    if(customer.getCustomerOrders().size() > 0){
                        for (CustomerOrder co : customer.getCustomerOrders()){
                            co.setCompany(null);
                            co = customerOrderRepository.save(co);
                        }
                    }
                }
            }
            customer.setName(dto.getName());
            customer.setSurname(dto.getSurname());
            customer.setCity(dto.getCity());
            customer.setJobTitle(dto.getJobTitle());
            customer.setEposta(dto.getEposta());
            customer.setBusinessPhone(dto.getBusinessPhone());
            customer.setMobilePhone(dto.getMobilePhone());
            customer.setMobilePhoneExtra(dto.getMobilePhoneExtra());
            customer.setPostalCode(dto.getPostalCode());
            customer.setCustomerFax(dto.getCustomerFax());
            customer.setOpenAddress(dto.getOpenAddress());
            customer = customerRepository.save(customer);


            /* Cari Kart Oluşturma Başladı */
            if(customer.getCompany() == null) {
                Optional<CheckingCard> optCheckingCard = checkingCardRepository.findByCheckingCardName(checkingCardName);

                if(optCheckingCard.isPresent()) {
                    //Önceden Kaydı Varsa Güncellenir
                    CheckingCard checkingCard = optCheckingCard.get();
                    checkingCard.setCheckingCardName(customer.getName() + " " + customer.getSurname());
                    checkingCard.setCountry(customer.getCountry());
                    checkingCard.setCity(customer.getCity());
                    checkingCard.setAddress(customer.getOpenAddress());
                    checkingCard.setEmail(customer.getEposta());
                    checkingCard.setPhoneNumber(customer.getBusinessPhone());
                    checkingCard.setFaxNumber(customer.getCustomerFax());
                    checkingCard.setType(CheckingCardType.CUSTOMER);
                    checkingCard.setSalesRepresentative(String.valueOf(customer.getUser().getUserId()));
                    checkingCard.setUser(user);
                    checkingCard.setCustomerId(customer.getCustomerId());
                    checkingCard.setCreatedAt(new Date());
                    checkingCard = checkingCardRepository.save(checkingCard);
                }else if(!optCheckingCard.isPresent()){
                    //Önceden Kaydı Yoksa Yeni Kayıt Eklenir
                    CheckingCard checkingCard = new CheckingCard();
                    checkingCard.setCheckingCardName(customer.getName() + " " + customer.getSurname());
                    checkingCard.setCountry(customer.getCountry());
                    checkingCard.setCity(customer.getCity());
                    checkingCard.setAddress(customer.getOpenAddress());
                    checkingCard.setEmail(customer.getEposta());
                    checkingCard.setPhoneNumber(customer.getBusinessPhone());
                    checkingCard.setFaxNumber(customer.getCustomerFax());
                    checkingCard.setType(CheckingCardType.CUSTOMER);
                    checkingCard.setSalesRepresentative(String.valueOf(customer.getUser().getUserId()));
                    checkingCard.setUser(user);
                    checkingCard.setCustomerId(customer.getCustomerId());
                    checkingCard.setCreatedAt(new Date());
                    checkingCard = checkingCardRepository.save(checkingCard);
                }
            }
            /* Cari Kart Oluşturma Bitti */

            return true;
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public List<CustomerDto> getAll(String authHeader) throws NotFoundException {
        List<CustomerDto> list = new ArrayList<>();
        try {
            User user = this.getUserFromToken(authHeader);
            if (user.getRole() == Role.ADMIN) {
                List<Customer> customers = customerRepository.findAll();
                CustomerDto[] array = mapper.map(customers, CustomerDto[].class);
                List<CustomerDto> dtos = Arrays.asList(array);
                return dtos;
            } else if (user.getRole() == Role.EXPORTER) {
                List<Customer> customers = customerRepository.findByUser(user);
                CustomerDto[] array = mapper.map(customers, CustomerDto[].class);
                List<CustomerDto> dtos = Arrays.asList(array);
                return dtos;
            }
        } catch (Exception e) {
            throw e;
        }
        return null;
    }

    @Override
    public User getUserFromToken(String authHeader) throws NotFoundException {

        String username = controlService.getUsernameFromToken(authHeader);
        Optional<User> optUser = userRepository.findByUsername(username);
        if (!optUser.isPresent()) {
            throw new NotFoundException("Not found User");
        }
        return optUser.get();
    }

    @Override
    public Page<CustomerDto> getAllWithPage(String authHeader, Pageable page, SearchCustomerDto dto) throws Exception {
        Page<CustomerDto> pageList = null;
        CustomerDto[] dtos = null;
        List<Object> list = new ArrayList<>();
        StringBuilder createSqlQuery = null;
        User user = this.getUserFromToken(authHeader);
        if (user.getRole() == Role.ADMIN) {
            createSqlQuery = new StringBuilder("select * from customer where status=1 ");
            if (dto.getName() != null)          createSqlQuery.append("and name ILIKE  '%" + dto.getName() + "%' ");
            if (dto.getSurname() != null)       createSqlQuery.append("and surname ILIKE  '%" + dto.getSurname() + "%' ");
            if (dto.getCountryId() != null)     createSqlQuery.append("and countryid = " + dto.getCountryId() + " ");
            if (dto.getUserId() != null)		createSqlQuery.append("and  userid = "+dto.getUserId()+" ");
            if (dto.getCompanyId() != null)     createSqlQuery.append("and  companyid = " + dto.getCompanyId() + " ");
            if (dto.getCity() != null)          createSqlQuery.append("and  city ILIKE  '%" + dto.getCity() + "%' ");
            if (dto.getCreatedDate() != null)   createSqlQuery.append("and  created_date = " + dto.getCreatedDate() + " ");
            if(page.getPageNumber()==0){
                createSqlQuery.append("order by name limit "+page.getPageSize()+" offset "+page.getPageNumber());
            }else{
                createSqlQuery.append("order by name limit "+page.getPageSize()+" offset "+(page.getPageSize()*page.getPageNumber()));
            }

        } else if (user.getRole() == Role.EXPORTER) {
            createSqlQuery = new StringBuilder("select * from customer where status=1 and userid ="+user.getUserId()+" ");
            if (dto.getCompanyId() != null)     createSqlQuery.append("and companyid ILIKE  '%"+dto.getCompanyId()+"%' ");
            if (dto.getCountryId() != null)     createSqlQuery.append("and countryid = "+dto.getCountryId()+" ");
            if (dto.getName() != null)          createSqlQuery.append("and name ILIKE  '%" + dto.getName() + "%' ");
            if (dto.getSurname() != null)       createSqlQuery.append("and surname ILIKE  '%" + dto.getSurname() + "%' ");
            if (dto.getCity() != null)          createSqlQuery.append("and  city ILIKE  '%" + dto.getCity() + "%' ");
            if (dto.getCreatedDate() != null)   createSqlQuery.append("and  created_date = " + dto.getCreatedDate() + " ");
            if(page.getPageNumber()==0){
                createSqlQuery.append("order by name limit "+page.getPageSize()+" offset "+page.getPageNumber());
            }else{
                createSqlQuery.append("order by name limit "+page.getPageSize()+" offset "+(page.getPageSize()*page.getPageNumber()));
            }
        }


        list = entityManager.createNativeQuery(createSqlQuery.toString(), Customer.class).getResultList();
        dtos = mapper.map(list, CustomerDto[].class);
        List<CustomerDto> dtosList = Arrays.asList(dtos);


//        int start = Math.min((int) page.getOffset(), dtosList.size());
//        int end = Math.min((start + page.getPageSize()), dtosList.size());

        int start=0;
        int end=dtosList.size();
        int totalCount=0;

        if (user.getRole() == Role.ADMIN) {
            createSqlQuery = new StringBuilder("select count(*) from customer where status=1 ");
            if (dto.getName() != null)          createSqlQuery.append("and name ILIKE  '%" + dto.getName() + "%' ");
            if (dto.getSurname() != null)       createSqlQuery.append("and surname ILIKE  '%" + dto.getSurname() + "%' ");
            if (dto.getCountryId() != null)     createSqlQuery.append("and countryid = " + dto.getCountryId() + " ");
            if (dto.getUserId() != null)		createSqlQuery.append("and  userid = "+dto.getUserId()+" ");
            if (dto.getCompanyId() != null)     createSqlQuery.append("and  companyid = " + dto.getCompanyId() + " ");
            if (dto.getCity() != null)          createSqlQuery.append("and  city ILIKE  '%" + dto.getCity() + "%' ");
            if (dto.getCreatedDate() != null)   createSqlQuery.append("and  created_date = " + dto.getCreatedDate() + " ");
        } else if (user.getRole() == Role.EXPORTER) {
            createSqlQuery = new StringBuilder("select count(*) from customer where status=1 and userid ="+user.getUserId()+" ");
            if (dto.getCompanyId() != null)     createSqlQuery.append("and companyid ILIKE  '%"+dto.getCompanyId()+"%' ");
            if (dto.getCountryId() != null)     createSqlQuery.append("and countryid = "+dto.getCountryId()+" ");
            if (dto.getName() != null)          createSqlQuery.append("and name ILIKE  '%" + dto.getName() + "%' ");
            if (dto.getSurname() != null)       createSqlQuery.append("and surname ILIKE  '%" + dto.getSurname() + "%' ");
            if (dto.getCity() != null)          createSqlQuery.append("and  city ILIKE  '%" + dto.getCity() + "%' ");
            if (dto.getCreatedDate() != null)   createSqlQuery.append("and  created_date = " + dto.getCreatedDate() + " ");
        }

        List<Object> countList = entityManager.createNativeQuery(createSqlQuery.toString()).getResultList();
        for(Object data :countList){
            totalCount= Integer.valueOf(String.valueOf((BigInteger) data));
        }

        pageList = new PageImpl<>(dtosList.subList(start, end), page, totalCount);
        return pageList;
    }


    @Override
    public List<CustomerDto> search(SingleCustomerDto dto) {
        StringBuilder createSqlQuery = new StringBuilder("select * from customer where status=1 ");
        if (dto.getName() != null)          createSqlQuery.append("and name ILIKE  '%" + dto.getName() + "%' ");
        if (dto.getSurname() != null)       createSqlQuery.append("and surname ILIKE  '%" + dto.getSurname() + "%' ");
        if (dto.getCountryId() != null)     createSqlQuery.append("and countryid = " + dto.getCountryId() + " ");
        if (dto.getCompanyId() != null)     createSqlQuery.append("and  companyid = " + dto.getCompanyId() + " ");
        if (dto.getCity() != null)          createSqlQuery.append("and  city ILIKE  '%" + dto.getCity() + "%' ");
        if (dto.getCreatedDate() != null)   createSqlQuery.append("and  created_date = " + dto.getCreatedDate() + " ");

        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Customer.class).getResultList();

        CustomerDto[] dtos = mapper.map(list, CustomerDto[].class);

        return Arrays.asList(dtos);
    }

    @Override
    public List<CustomerDto> findByCustomerSearching(String authHeader,String customerName) throws NotFoundException {

        List<CustomerDto> dtoList = new ArrayList<>();
        CustomerDto[] dtos = null;
        User user = this.getUserFromToken(authHeader);
        if(customerName.length()>1){
            if(user.getRole() == Role.ADMIN){
                StringBuilder createSqlQuery = new StringBuilder("select * from customer where status=1 and (name ILIKE '%"+ customerName +"%' or surname ILIKE '%"+ customerName+"%' )");
                List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Customer.class).getResultList();
                dtos = mapper.map(list,CustomerDto[].class );
            }  else if(user.getRole() == Role.EXPORTER){
                StringBuilder createSqlQuery = new StringBuilder("select * from customer where status=1 and (name ILIKE '%"+ customerName +"%' or surname ILIKE '%"+customerName+"%' ) and userid="+user.getUserId());
                List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Customer.class).getResultList();
                dtos = mapper.map(list,CustomerDto[].class );
            }
        }
        dtoList = Arrays.asList(dtos);
        return dtoList;
    }

}
