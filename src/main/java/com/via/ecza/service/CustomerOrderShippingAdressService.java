package com.via.ecza.service;

import com.via.ecza.dto.CustomerOrderShippingAdressDto;
import com.via.ecza.dto.CustomerOrderShippingAdressSaveDto;
import com.via.ecza.entity.*;
import com.via.ecza.entity.enumClass.Role;
import com.via.ecza.repo.CountryRepository;
import com.via.ecza.repo.CustomerOrderRepository;
import com.via.ecza.repo.CustomerOrderShippingAdressRepository;
import com.via.ecza.repo.UserRepository;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class CustomerOrderShippingAdressService {

    @Autowired
    private CustomerOrderShippingAdressRepository customerOrderShippingAdressRepository;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private CustomerOrderRepository customerOrderRepository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ControlService controlService;

    private User getUserFromToken(String authHeader) throws NotFoundException {

        String username = controlService.getUsernameFromToken(authHeader);
        Optional<User> optUser = userRepository.findByUsername(username);
        if (!optUser.isPresent()) {
            throw new NotFoundException("Not found User");
        }
        return optUser.get();
    }

    public  Boolean save(String authHeader, CustomerOrderShippingAdressSaveDto dto) throws Exception {

        if(dto.getCustomerOrderId() == null )
            return false;
        User user = this.getUserFromToken(authHeader);
        Optional<CustomerOrder> optionalCustomerOrder = null;
        Optional<Country> optionalCountry = null;
        if (user.getRole() == Role.ADMIN) {
            optionalCustomerOrder = customerOrderRepository.findById(dto.getCustomerOrderId());
            if (!optionalCustomerOrder.isPresent())  throw new NotFoundException("Sipariş Kaydı bulunamadı");
            if (dto.getCountryId() == null)    return false;
            optionalCountry  = countryRepository.findById(dto.getCountryId());
            if (!optionalCountry.isPresent()) return false;
        }
        else if (user.getRole() == Role.EXPORTER) {
            optionalCustomerOrder = customerOrderRepository.getSingleCustomerOrderForExporter(dto.getCustomerOrderId(), user.getUserId());
            if (!optionalCustomerOrder.isPresent())  throw new NotFoundException("Sipariş Kaydı bulunamadı");
            if (dto.getCountryId() == null)    return false;
            optionalCountry  = countryRepository.findById(dto.getCountryId());
            if (!optionalCountry.isPresent())   return false;
        }
        CustomerOrder customerOrder = optionalCustomerOrder.get();
        CustomerOrderShippingAdress customerOrderShippingAdress = new CustomerOrderShippingAdress();
        if(customerOrder.getCustomerOrderShippingAdress() != null)
            customerOrderShippingAdress = customerOrder.getCustomerOrderShippingAdress();
        customerOrderShippingAdress.setCountry(optionalCountry.get());
        customerOrderShippingAdress.setCustomerOrder(customerOrder);
        customerOrderShippingAdress.setCompanyName(dto.getCompanyName());
        customerOrderShippingAdress.setContactName(dto.getContactName());
        customerOrderShippingAdress.setFullAddress(dto.getFullAddress());
        customerOrderShippingAdress.setCity(dto.getCity());
        customerOrderShippingAdress.setPhone(dto.getPhone());
        customerOrderShippingAdress.setEmail(dto.getEmail());
        customerOrderShippingAdress.setCreatedDate(new Date());
        customerOrderShippingAdress = customerOrderShippingAdressRepository.save(customerOrderShippingAdress);
        return true;
    }

    public CustomerOrderShippingAdressDto findByCustomerOrderId(String authHeader, Long customerOrderId) throws Exception  {
        CustomerOrderShippingAdressDto dto = null ;
        Optional<CustomerOrder> co= customerOrderRepository.findById(customerOrderId);
        if(!co.isPresent())
            throw new NotFoundException("Böyle bir sipariş yoktur");
        Optional<CustomerOrderShippingAdress> ship =  customerOrderShippingAdressRepository.findByCustomerOrder(co.get());
        dto = mapper.map(ship.get(),CustomerOrderShippingAdressDto.class );

        return dto;
    }
}
