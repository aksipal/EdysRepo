package com.via.ecza.manager.service;

import com.via.ecza.dto.CustomerDto;
import com.via.ecza.dto.CustomerSaveDto;
import com.via.ecza.dto.SearchCustomerDto;
import com.via.ecza.dto.SingleCustomerDto;
import com.via.ecza.entity.User;
import javassist.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.validation.Valid;
import java.util.List;

public interface CustomerService {

    Boolean save(String authHeader, @Valid CustomerSaveDto dto);
    CustomerDto findById(String authHeader, Long customerId) throws NotFoundException;
    Boolean update(String authHeader, Long customerId, CustomerSaveDto dto) throws Exception;
    List<CustomerDto> getAll(String authHeader) throws NotFoundException;
    User getUserFromToken(String authHeader) throws NotFoundException;
    Page<CustomerDto> getAllWithPage(String authHeader, Pageable page, SearchCustomerDto dto) throws Exception;
    List<CustomerDto> findByCustomerSearching(String authHeader,String customerName) throws NotFoundException;
    List<CustomerDto> search(SingleCustomerDto dto);



}
