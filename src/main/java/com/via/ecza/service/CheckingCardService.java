package com.via.ecza.service;


import com.via.ecza.dto.*;
import com.via.ecza.entity.CheckingCard;
import com.via.ecza.entity.CustomerOrder;
import com.via.ecza.entity.User;
import com.via.ecza.repo.CheckingCardRepository;
import com.via.ecza.repo.CountryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.*;

@Service
@Transactional
public class CheckingCardService {

    @Autowired
    private CheckingCardRepository checkingCardRepository;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ControlService controlService;

    public Boolean save(String authHeader, @Valid CheckingCardSaveDto dto) throws Exception {
        try {

            User user = controlService.getUserFromToken(authHeader);


            CheckingCard checkingCard = mapper.map(dto, CheckingCard.class);
            checkingCard.setCheckingCardId(null);
            if (dto.getCountryId() != null) {
                checkingCard.setCountry(countryRepository.findById(dto.getCountryId()).get());
            }
            if (user != null) {
                checkingCard.setUser(user);
            }

            checkingCard.setCreatedAt(new Date());

            checkingCardRepository.save(checkingCard);


            return true;
        } catch (Exception e) {
            throw e;
        }
    }

    public Page<CheckingCardDto> search(CheckingCardSearchDto dto, Integer pageNo, Integer pageSize, String sortBy) {

        StringBuilder createSqlQuery = new StringBuilder("select * from checking_card cc where 1=1 ");

        if (dto.getCheckingCardName() != null && dto.getCheckingCardName().trim().length()>0)
            createSqlQuery.append("and cc.checking_card_name ILIKE  '%" + dto.getCheckingCardName().trim() + "%' ");
        if (dto.getCountryId() != null)
            createSqlQuery.append("and cc.countryid =  " + dto.getCountryId() + " ");
        if (dto.getCity() != null && dto.getCity().trim().length()>0)
            createSqlQuery.append("and cc.city ILIKE  '%" + dto.getCity().trim() + "%' ");
        if (dto.getTaxOffice() != null && dto.getTaxOffice().trim().length()>0)
            createSqlQuery.append("and cc.tax_office ILIKE  '%" + dto.getTaxOffice().trim() + "%' ");
        if (dto.getTaxIdentificationNumber() != null)
            createSqlQuery.append("and cc.tax_identification_number =  " + dto.getTaxIdentificationNumber() + " ");
        if (dto.getType() != null && dto.getType().trim().length()>0)
            createSqlQuery.append("and cc.type =  '" + dto.getType() + "' ");

        createSqlQuery.append(" order by cc.checking_card_id");

        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), CheckingCard.class).getResultList();
        CheckingCardDto[] dtos = mapper.map(list, CheckingCardDto[].class);
        List<CheckingCardDto> dtosList = Arrays.asList(dtos);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
        int start = Math.min((int) paging.getOffset(), dtosList.size());
        int end = Math.min((start + paging.getPageSize()), dtosList.size());

        Page<CheckingCardDto> pageList = new PageImpl<>(dtosList.subList(start, end), paging, dtosList.size());

        return pageList;

    }

    public CheckingCardDto getCheckingCardDetail(Long checkingCardId) throws Exception {
        Optional<CheckingCard> checkingCard = checkingCardRepository.findById(checkingCardId);
        if (!checkingCard.isPresent()) {
            throw new Exception("Cari Kart Bulunamadı !");
        }
        CheckingCardDto dto = mapper.map(checkingCard.get(), CheckingCardDto.class);
        return dto;

    }

    public Boolean update(String authHeader, @Valid CheckingCardUpdateDto dto) throws Exception {
        try {

            User user = controlService.getUserFromToken(authHeader);


            Optional<CheckingCard> optCheckingCard = checkingCardRepository.findById(dto.getCheckingCardId());
            if (!optCheckingCard.isPresent()) {
                throw new Exception("Cari Kart Bulunamadı !");
            }

            CheckingCard checkingCard = mapper.map(dto, CheckingCard.class);
            checkingCard.setCheckingCardId(optCheckingCard.get().getCheckingCardId());


            if (dto.getCountryId() != null) {
                checkingCard.setCountry(countryRepository.findById(dto.getCountryId()).get());
            }
            if (user != null) {
                checkingCard.setUser(user);
            }

            checkingCard.setCreatedAt(new Date());
            checkingCardRepository.save(checkingCard);
            return true;
        } catch (Exception e) {
            throw e;
        }
    }

    public List<CheckingCardDto> findBySearching(String checkingCardName) {
        List<CheckingCardDto> dtoList = new ArrayList<>();
        if(checkingCardName.length()>2){
            StringBuilder createSqlQuery = new StringBuilder("select * from checking_card cc where cc.checking_card_name ILIKE '%"+checkingCardName+"%' ");
            List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), CheckingCard.class).getResultList();
            CheckingCardDto[] dtos = mapper.map(list,CheckingCardDto[].class );
            dtoList = Arrays.asList(dtos);
            return dtoList;
        }else{
            return dtoList;
        }
    }

    public List<CheckingCardDto> findCustomerBySearching(String checkingCardName) {
        List<CheckingCardDto> dtoList = new ArrayList<>();
        if(checkingCardName.length()>1){
            StringBuilder createSqlQuery = new StringBuilder("select * from checking_card cc where cc.checking_card_name ILIKE '%"+checkingCardName+"%' ");
            createSqlQuery.append("and (cc.customer_id is not null or cc.company_id is not null)");
            List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), CheckingCard.class).getResultList();
            CheckingCardDto[] dtos = mapper.map(list,CheckingCardDto[].class );
            dtoList = Arrays.asList(dtos);
            return dtoList;
        }else{
            return dtoList;
        }
    }

    public List<CheckingCardDto> findSupplierBySearching(String checkingCardName) {
        List<CheckingCardDto> dtoList = new ArrayList<>();
        if(checkingCardName.length()>1){
            StringBuilder createSqlQuery = new StringBuilder("select * from checking_card where checking_card_name ILIKE '%"+checkingCardName+"%' and type='SUPPLIER'");

            List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), CheckingCard.class).getResultList();
            CheckingCardDto[] dtos = mapper.map(list,CheckingCardDto[].class );
            dtoList = Arrays.asList(dtos);
            return dtoList;
        }else{
            return dtoList;
        }
    }

    public List<CheckingCardCustomerOrderDto> getCustomerOrdersByCheckingCard(CheckingCardSearchDto dto) {

        StringBuilder createSqlQuery = new StringBuilder("select * from customer_order co " +
                "join customer c on c.customer_id = co.customer_id  " +
                //"left join customer_receipt cr on cr.customer_id = c.customer_id " +
                "where co.order_status_id = 50 ");
        createSqlQuery.append("and (co.companyid = "+dto.getCompanyId()+" or co.customer_id = "+dto.getCustomerId()+" ) ");
        //createSqlQuery.append("and cr.invoice_id is null");

        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), CustomerOrder.class).getResultList();
        CheckingCardCustomerOrderDto[] dtos = mapper.map(list,CheckingCardCustomerOrderDto[].class );
        List<CheckingCardCustomerOrderDto> dtoList = Arrays.asList(dtos);

        return dtoList;
    }

    public List<CheckingCardDto> getAllCheckingCards() {

        List<CheckingCard> list = checkingCardRepository.getAllCheckingCards();

        CheckingCardDto[] dtos = mapper.map(list, CheckingCardDto[].class);
        List<CheckingCardDto> dtosList = Arrays.asList(dtos);
        return dtosList;
    }

}
