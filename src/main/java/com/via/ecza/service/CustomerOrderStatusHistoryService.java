package com.via.ecza.service;

import com.via.ecza.dto.CustomerOrderStatusHistoryDto;
import com.via.ecza.entity.CustomerOrder;
import com.via.ecza.entity.CustomerOrderStatus;
import com.via.ecza.entity.CustomerOrderStatusHistory;
import com.via.ecza.repo.CustomerOrderRepository;
import com.via.ecza.repo.CustomerOrderStatusHistoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CustomerOrderStatusHistoryService {

    @Autowired
    private CustomerOrderStatusHistoryRepository customerOrderStatusHistoryRepository;
    @Autowired
    private CustomerOrderRepository customerOrderRepository;
    @Autowired
    private ModelMapper mapper;

    public Boolean save(CustomerOrder customerOrder, CustomerOrderStatus customerOrderStatus)  throws Exception{

        CustomerOrderStatusHistory customerOrderStatusHistory = new CustomerOrderStatusHistory();
        customerOrderStatusHistory.setCustomerOrder(customerOrder);
        customerOrderStatusHistory.setCustomerOrderStatus(customerOrderStatus);
        customerOrderStatusHistory.setCreatedDate(new Date());
        customerOrderStatusHistory = customerOrderStatusHistoryRepository.save(customerOrderStatusHistory);

        return true;
    }


    public List<CustomerOrderStatusHistoryDto> getAllOrderStatus(Long customerOrderId) throws Exception {

        Optional<CustomerOrder> optionalCustomerOrder = customerOrderRepository.findById(customerOrderId);
        if(!optionalCustomerOrder.isPresent())
            throw new Exception("Sipariş Bulunamadı");

        List<CustomerOrderStatusHistory> customerOrderStatusHistoryList = customerOrderStatusHistoryRepository.findByCustomerOrder(optionalCustomerOrder.get());
        if(!(customerOrderStatusHistoryList.size()>0))
            throw new Exception("Statü Bulunamadı");

        CustomerOrderStatusHistoryDto[] list = mapper.map(customerOrderStatusHistoryList, CustomerOrderStatusHistoryDto[].class);

        return Arrays.asList(list);
    }
}
