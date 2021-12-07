package com.via.ecza.service;

import com.via.ecza.dto.CustomerOrderBankDetailDto;
import com.via.ecza.dto.CustomerOrderBankDetailSaveDto;
import com.via.ecza.dto.CustomerOrderShippingAdressDto;
import com.via.ecza.entity.*;
import com.via.ecza.entity.enumClass.Role;
import com.via.ecza.repo.CustomerOrderBankDetailRepository;
import com.via.ecza.repo.CustomerOrderRepository;
import com.via.ecza.repo.UserRepository;
import javassist.NotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.util.Optional;

@Service
public class CustomerOrderBankDetailService {

    @Autowired
    private ControlService controlService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CustomerOrderBankDetailRepository customerOrderBankDetailRepository;
    @Autowired
    private CustomerOrderRepository customerOrderRepository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private EntityManager entityManager;

    private User getUserFromToken(String authHeader) throws NotFoundException {

        String username = controlService.getUsernameFromToken(authHeader);
        Optional<User> optUser = userRepository.findByUsername(username);
        if (!optUser.isPresent()) {
            throw new NotFoundException("Not found User");
        }
        return optUser.get();
    }


    public Boolean saveBankDetail(String authHeader, CustomerOrderBankDetailSaveDto dto) throws Exception {
        try {
            Optional<CustomerOrder> optionalCustomerOrder= null;
            if(dto.getCustomerOrderId() == null )
                return false;
            User user = this.getUserFromToken(authHeader);
            if (user.getRole() == Role.ADMIN) {
                optionalCustomerOrder = customerOrderRepository.findById(dto.getCustomerOrderId());
                if (!optionalCustomerOrder.isPresent()) {
                    throw new NotFoundException("Sipariş Kaydı bulunamadı");
                }
                CustomerOrderBankDetail customerOrderBankDetail = new CustomerOrderBankDetail();
                if(optionalCustomerOrder.get().getCustomerOrderBankDetail() != null)
                    customerOrderBankDetail = optionalCustomerOrder.get().getCustomerOrderBankDetail();
                customerOrderBankDetail.setCustomerOrder(optionalCustomerOrder.get());
                customerOrderBankDetail.setAccountName(dto.getAccountName());
                customerOrderBankDetail.setBankName(dto.getBankName());
                customerOrderBankDetail.setIbanNo(dto.getIbanNo());
                customerOrderBankDetail.setSwift(dto.getSwift());
                customerOrderBankDetail = customerOrderBankDetailRepository.save(customerOrderBankDetail);

                return true;

            } else if (user.getRole() == Role.EXPORTER) {
                optionalCustomerOrder = customerOrderRepository.getSingleCustomerOrderForExporter(dto.getCustomerOrderId(), user.getUserId());
                if (!optionalCustomerOrder.isPresent()) {
                    throw new NotFoundException("Sipariş Kaydı bulunamadı");
                }
                CustomerOrderBankDetail customerOrderBankDetail = new CustomerOrderBankDetail();
                if(optionalCustomerOrder.get().getCustomerOrderBankDetail() != null)
                    customerOrderBankDetail = optionalCustomerOrder.get().getCustomerOrderBankDetail();
                customerOrderBankDetail.setCustomerOrder(optionalCustomerOrder.get());
                customerOrderBankDetail.setAccountName(dto.getAccountName());
                customerOrderBankDetail.setBankName(dto.getBankName());
                customerOrderBankDetail.setIbanNo(dto.getIbanNo());
                customerOrderBankDetail.setSwift(dto.getSwift());
                customerOrderBankDetail = customerOrderBankDetailRepository.save(customerOrderBankDetail);

                return true;
            }

        } catch (Exception e) {
            throw e;

        }
        return null;
    }

    public CustomerOrderBankDetailDto findByCustomerOrderId(String authHeader, Long customerOrderId) throws NotFoundException {
        CustomerOrderBankDetailDto dto = null ;

        Optional<CustomerOrder> co= customerOrderRepository.findById(customerOrderId);
        if(!co.isPresent())
            throw new NotFoundException("Böyle bir sipariş yoktur");
        Optional<CustomerOrderBankDetail> ship =  customerOrderBankDetailRepository.findByCustomerOrder(co.get());
        dto = mapper.map(ship.get(), CustomerOrderBankDetailDto.class );

        return dto;
    }


}
