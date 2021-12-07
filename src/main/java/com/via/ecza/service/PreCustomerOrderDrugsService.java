package com.via.ecza.service;

import com.via.ecza.dto.*;
import com.via.ecza.entity.*;
import com.via.ecza.entity.enumClass.Role;
import com.via.ecza.repo.*;
import javassist.NotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PreCustomerOrderDrugsService {

    @Autowired
    private PreCustomerOrderDrugsRepository preCustomerOrderDrugsRepository;
    @Autowired
    private CustomerOrderRepository customerOrderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private ControlService controlService;
    @Autowired
    private DrugCardRepository drugCardRepository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private CustomerOrderDrugsService customerOrderDrugsService;
    public Boolean preDrugSave(CustomerOrderDrugsSaveDto dto, String authHeader) throws Exception {

        User user = controlService.getUserFromToken(authHeader);

        Optional<DrugCard> optDrugCard = drugCardRepository.findById(dto.getDrugCardId());
        if (!optDrugCard.isPresent()) {
            throw new NotFoundException("İlaç  Bulunamadı..");
        }

        Optional<CustomerOrder> optionalCustomerOrder = customerOrderRepository.findById(dto.getCustomerOrderId());
        if (!optionalCustomerOrder.isPresent()) {
            throw new NotFoundException("Yurt Dışı Sipariş Bulunamadı");
        }
        CustomerOrder customerOrder = optionalCustomerOrder.get();

        Optional<Customer> optionalCustomer = customerRepository.findById(dto.getCustomerId());
        if (!optionalCustomer.isPresent()) {
            throw new NotFoundException("Müşteri Bulunamadı");
        }
        if (optionalCustomer.get().getCustomerId() != optionalCustomerOrder.get().getCustomer().getCustomerId()) {
            throw new Exception("Siparişe Bağlı Müşteri Kaydı Uyuşmadı");
        }

        PreCustomerOrderDrugs preCustomerOrderDrugs = new PreCustomerOrderDrugs();
        preCustomerOrderDrugs.setDrugCard(optDrugCard.get());
        preCustomerOrderDrugs.setCustomerOrder(customerOrder);
        preCustomerOrderDrugs.setStatus(1);
        preCustomerOrderDrugs.setCustomerOrderDrugNote(dto.getCustomerOrderDrugNote());
        preCustomerOrderDrugs.setExpirationDate(dto.getExpirationDate());
        preCustomerOrderDrugs.setTotalQuantity(dto.getTotalQuantity());

        if (user.getRole() == Role.EXPORTER)
            preCustomerOrderDrugs.setUser(user);

        else if(user.getRole() == Role.ADMIN)
            preCustomerOrderDrugs.setUser(customerOrder.getUser());

        else
            return false;

        preCustomerOrderDrugs = preCustomerOrderDrugsRepository.save(preCustomerOrderDrugs);
        return true;
    }

    public Boolean preDrugDelete(String authHeader, CustomerOrderDrugDeleteDto dto) throws Exception {

        User user = controlService.getUserFromToken(authHeader);
        Optional<CustomerOrder> optionalCustomerOrder = customerOrderRepository.findById(dto.getCustomerOrderId());
        if (!optionalCustomerOrder.isPresent()) {
            throw new Exception("Böyle bir sipariş yoktur.");
        }
        Optional<PreCustomerOrderDrugs> optionalPreCustomerOrderDrugs = null;

        if (user.getRole() == Role.EXPORTER)
            optionalPreCustomerOrderDrugs = preCustomerOrderDrugsRepository.findByPreCustomerOrderDrugIdAndCustomerOrderAndUser(
                    dto.getPreCustomerOrderDrugId(), optionalCustomerOrder.get(), user);
        else if(user.getRole() == Role.ADMIN)
            optionalPreCustomerOrderDrugs = preCustomerOrderDrugsRepository.findByPreCustomerOrderDrugIdAndCustomerOrder(
                    dto.getPreCustomerOrderDrugId(), optionalCustomerOrder.get());
        else
            return false;

        if (!optionalPreCustomerOrderDrugs.isPresent()) {
            throw new Exception("Böyle bir sipariş yoktur.");
        }
        PreCustomerOrderDrugs preCustomerOrderDrugs = optionalPreCustomerOrderDrugs.get();
        preCustomerOrderDrugs.setDrugCard(null);
        preCustomerOrderDrugs.setCustomerOrder(null);
        preCustomerOrderDrugs.setUser(null);
        preCustomerOrderDrugsRepository.delete(preCustomerOrderDrugs);
        return true;
    }

    public List<PreCustomerOrderDrugsListDto> preDrugGetAll(String authHeader, PreCustomerOrderDrugsSelectDto dto) throws Exception {
        PreCustomerOrderDrugsListDto[] dtos = null;
        Optional<CustomerOrder> optionalCustomerOrder = customerOrderRepository.findById(dto.getCustomerOrderId());
        if (!optionalCustomerOrder.isPresent()) {
            throw new Exception("Böyle bir sipariş yoktur.");
        }

        List<PreCustomerOrderDrugs> preCustomerOrderDrugsList = preCustomerOrderDrugsRepository.findByCustomerOrder(optionalCustomerOrder.get());
        if (!(preCustomerOrderDrugsList.size()>0)) {
            return null;
        }

         dtos = mapper.map(preCustomerOrderDrugsList, PreCustomerOrderDrugsListDto[].class);
        return Arrays.asList(dtos);
    }

    public Boolean preDrugSaveAll(String authHeader, PreCustomerOrderDrugsSelectDto dto) throws Exception {

        User user = controlService.getUserFromToken(authHeader);
        Optional<CustomerOrder> optionalCustomerOrder = customerOrderRepository.findById(dto.getCustomerOrderId());
        if (!optionalCustomerOrder.isPresent()) {
            throw new Exception("Böyle bir sipariş yoktur.");
        }
        Optional<PreCustomerOrderDrugs> optionalPreCustomerOrderDrugs = null;


        List<PreCustomerOrderDrugs> preCustomerOrderDrugsList = preCustomerOrderDrugsRepository.findByCustomerOrder(optionalCustomerOrder.get());
        if (!(preCustomerOrderDrugsList.size()>0)) {
            throw new Exception("Böyle bir sipariş yoktur");
        }

        Boolean control = true;
        if (user.getRole() == Role.EXPORTER || user.getRole() == Role.ADMIN) {
            for ( PreCustomerOrderDrugs drug : preCustomerOrderDrugsList){
                CustomerOrderDrugsSaveDto saveDto = new CustomerOrderDrugsSaveDto();
                saveDto.setDrugCardId(drug.getDrugCard().getDrugCardId());
                saveDto.setCreatedDate(new Date());
                saveDto.setTotalQuantity(drug.getTotalQuantity());
                saveDto.setExpirationDate(drug.getExpirationDate());
                saveDto.setCustomerOrderId(drug.getCustomerOrder().getCustomerOrderId());
                saveDto.setCustomerId(drug.getCustomerOrder().getCustomer().getCustomerId());
                control = customerOrderDrugsService.save(saveDto);
                if ( control)
                    preCustomerOrderDrugsRepository.delete(drug);
            }
        }
        else return false;

        return true;
    }
}
