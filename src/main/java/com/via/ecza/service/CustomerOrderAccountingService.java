package com.via.ecza.service;

import com.via.ecza.dto.CustomerOrderAccountingDto;
import com.via.ecza.dto.CustomerOrderAccountingSearchDto;
import com.via.ecza.dto.CustomerOrderListDto;
import com.via.ecza.entity.CustomerOrder;
import com.via.ecza.repo.CustomerOrderAccountingRepository;
import com.via.ecza.repo.CustomerOrderRepository;
import com.via.ecza.repo.UserRepository;
import javassist.NotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CustomerOrderAccountingService {

    @Autowired
    private CustomerOrderAccountingRepository customerOrderAccountingRepository;
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


    public Page<CustomerOrderListDto> search(CustomerOrderAccountingSearchDto dto, Integer pageNo, Integer pageSize, String sortBy) {

        List<CustomerOrderListDto> dtosList = null;
        StringBuilder createSqlQuery = new StringBuilder("select * from customer_order co ");


        createSqlQuery.append("join customer c on c.customer_id = co.customer_id  where co.status=1 ");
        if (dto.getCustomerId() != 0) {
            createSqlQuery.append("and c.customer_id = " + dto.getCustomerId() + " ");
        }
        if (dto.getCountryId() != 0) {
            createSqlQuery.append("and c.countryid = " + dto.getCountryId() + " ");
        }
        if (dto.getCity() != null) {
            createSqlQuery.append("and  c.city ILIKE '%" + dto.getCity() + "%' ");
        }
        if (dto.getCustomerOrderNo() != null)
            createSqlQuery.append("and  co.customer_order_no ILIKE '%" + dto.getCustomerOrderNo() + "%' ");


        createSqlQuery.append("and  co.order_status_id = 50");

        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), CustomerOrder.class).getResultList();

        CustomerOrderListDto[] dtos = mapper.map(list, CustomerOrderListDto[].class);
        dtosList = Arrays.asList(dtos);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
        int start = Math.min((int) paging.getOffset(), dtosList.size());
        int end = Math.min((start + paging.getPageSize()), dtosList.size());

        Page<CustomerOrderListDto> pageList = new PageImpl<>(dtosList.subList(start, end), paging, dtosList.size());

        return pageList;


    }

    public CustomerOrderAccountingDto findById(Long customerOrderId) throws NotFoundException {
        Optional<CustomerOrder> optionalCustomerOrder = customerOrderRepository.findById(customerOrderId);
        if(!optionalCustomerOrder.isPresent()) {
            throw new NotFoundException("Müşteri Siparişi Bulunamadı.");
        }
        CustomerOrderAccountingDto dto = mapper.map(optionalCustomerOrder.get(),CustomerOrderAccountingDto.class);
        return dto;
    }
}
