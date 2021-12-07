package com.via.ecza.manager;

import com.via.ecza.dto.CompanyDto;
import com.via.ecza.dto.SearchCompanyDto;
import com.via.ecza.dto.SingleCompanyDto;
import com.via.ecza.entity.*;
import com.via.ecza.entity.enumClass.CheckingCardType;
import com.via.ecza.entity.enumClass.Role;
import com.via.ecza.manager.service.CompanyService;
import com.via.ecza.repo.CheckingCardRepository;
import com.via.ecza.repo.CompanyRepository;
import com.via.ecza.repo.CountryRepository;
import com.via.ecza.repo.UserRepository;
import com.via.ecza.service.ControlService;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.validation.Valid;
import java.math.BigInteger;
import java.util.*;

@Slf4j
@Service
public class CompanyManager  implements CompanyService {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ControlService controlService;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private CheckingCardRepository checkingCardRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public Boolean save(String authHeader, @Valid CompanyDto dto) throws Exception {
        User user = controlService.getUserFromToken(authHeader);
        try {
            Optional<Country> optCountry = countryRepository.findById(dto.getCountryId());
            if(!optCountry.isPresent()) {
                log.info("Seçili Ülke Kaydı Bulunamadı..");
                throw new NotFoundException("Seçili Ülke Kaydı Bulunamadı..");
            }
            Company company  = mapper.map(dto,Company.class);

            //company.setUser(optUser.get());
            Country country = optCountry.get();
            company.setStatus(1);
            company.setUser(user);
            company.setCity(dto.getCity());
            company.setCompanyPhone(dto.getCompanyPhone());
            company.setCompanyMobilePhone(dto.getCompanyMobilePhone());
            company.setEmailAddress(dto.getEmailAddress());
            company.setCreatedDate(new Date());
            company.setCountry(country);
            company = companyRepository.save(company);


            /* Cari Kart Oluşturma Başladı */
            CheckingCard checkingCard = new CheckingCard();
            checkingCard.setCheckingCardName(company.getCompanyName());
            checkingCard.setCountry(company.getCountry());
            checkingCard.setCity(company.getCity());
            checkingCard.setAddress(company.getAddress());
            checkingCard.setEmail(company.getEmailAddress());
            checkingCard.setPhoneNumber(company.getCompanyPhone());
            checkingCard.setFaxNumber(company.getCompanyFax());
            if(company.getTaxNo()!=null && company.getTaxNo().trim().length()>0) {
                checkingCard.setTaxIdentificationNumber(Long.valueOf(company.getTaxNo().trim()));
            }
            checkingCard.setType(CheckingCardType.CUSTOMER);
            checkingCard.setSalesRepresentative(String.valueOf(company.getUser().getUserId()));
            checkingCard.setUser(user);
            checkingCard.setCompanyId(company.getCompanyId());
            checkingCard.setCreatedAt(new Date());
            checkingCard=checkingCardRepository.save(checkingCard);
            /* Cari Kart Oluşturma Bitti */



            if (dto.getCountryId() != null) {
                checkingCard.setCountry(countryRepository.findById(dto.getCountryId()).get());
            }
            if (user != null) {
                checkingCard.setUser(user);
            }

            checkingCard.setCreatedAt(new Date());

            checkingCardRepository.save(checkingCard);

            return true;
        } catch (Exception e){
            throw e;
        }
    }

    @Override
    public List<SingleCompanyDto> getAll(String authHeader ) throws NotFoundException {
        List<Company> list = null;

        User user = controlService.getUserFromToken(authHeader);
        if(user.getRole() == Role.ADMIN){
            list = companyRepository.findAll();
            if(list.size()<1){
                log.info("Şirket Kaydı Bulunamadı..");
                return null;
            }
        }else if(user.getRole() == Role.EXPORTER){
            list = companyRepository.findByUser(user);
            if(list.size()<1){
                log.info("Şirket Kaydı Bulunamadı..");
                return null;
            }
        }else{
            return null;
        }

        SingleCompanyDto[] array = mapper.map(list,SingleCompanyDto[].class );
        List<SingleCompanyDto> dtos = Arrays.asList(array);
        return dtos;
    }

    @Override
    public List<CompanyDto> findBySearching(String authHeader,String companyName) throws NotFoundException {

        List<CompanyDto> dtoList = new ArrayList<>();
        User user = controlService.getUserFromToken(authHeader);
        if(companyName.length()>1){

            if(user.getRole() == Role.ADMIN){
            StringBuilder createSqlQuery = new StringBuilder("select * from company where status=1 and company_name ILIKE '%"+companyName+"%' ");
            List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Company.class).getResultList();
            CompanyDto[] dtos = mapper.map(list,CompanyDto[].class );
            dtoList = Arrays.asList(dtos);
            return dtoList;
            }  else if(user.getRole() == Role.EXPORTER){

                StringBuilder createSqlQuery = new StringBuilder("select * from company where status=1 and company_name ILIKE '%"+companyName+"%' and userid="+user.getUserId());
                List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Company.class).getResultList();
                CompanyDto[] dtos = mapper.map(list,CompanyDto[].class );
                dtoList = Arrays.asList(dtos);
                return dtoList;
            }else{
                return dtoList;
            }
        }else{
            return dtoList;
        }
    }

    @Override
    public Page<SingleCompanyDto> getAllWithPagination(String authHeader, Pageable page,SearchCompanyDto dto) throws Exception {
        Page<SingleCompanyDto> pageList = null;
        SingleCompanyDto[] dtos = null;
        StringBuilder createSqlQuery = null;
        User user = controlService.getUserFromToken(authHeader);
        if(user.getRole() == Role.ADMIN){
            createSqlQuery = new StringBuilder("select * from company where status=1 ");
            if(dto.getCompanyName() != null)    createSqlQuery.append("and company_name ILIKE  '%"+dto.getCompanyName()+"%' ");
            if(dto.getCountryId() != null){
                Optional<Country> optCountry = countryRepository.findById(dto.getCountryId());
                if(!optCountry.isPresent()) {
                    log.info("Seçili Ülke Kaydı Bulunamadı..");
                }else{
                    createSqlQuery.append("and countryid = "+dto.getCountryId()+" ");
                }
            }
            if(dto.getCity() != null)			createSqlQuery.append("and  city ILIKE  '%"+dto.getCity()+"%' ");

            if(dto.getUserId() != null)			createSqlQuery.append("and  userid = "+dto.getUserId()+" ");

            if(dto.getCreatedDate() != null)	createSqlQuery.append("and  created_date = "+dto.getCreatedDate()+" ");

            if(page.getPageNumber()==0){
                createSqlQuery.append("order by company_name limit "+page.getPageSize()+" offset "+page.getPageNumber());
            }else{
                createSqlQuery.append("order by company_name limit "+page.getPageSize()+" offset "+(page.getPageSize()*page.getPageNumber()));
            }

            List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Company.class).getResultList();

            dtos = mapper.map(list,SingleCompanyDto[].class );

        } else if(user.getRole() == Role.EXPORTER){
            createSqlQuery = new StringBuilder("select * from company where status=1 and userid ="+user.getUserId()+" ");
            if(dto.getCompanyName() != null)    createSqlQuery.append("and company_name ILIKE  '%"+dto.getCompanyName()+"%' ");
            if(dto.getCountryId() != null){
                Optional<Country> optCountry = countryRepository.findById(dto.getCountryId());
                if(!optCountry.isPresent()) {
                    log.info("Seçili Ülke Kaydı Bulunamadı..");
                }else{
                    createSqlQuery.append("and countryid = "+dto.getCountryId()+" ");
                }
            }
            if(dto.getCity() != null)			createSqlQuery.append("and  city ILIKE  '%"+dto.getCity()+"%' ");

            if(dto.getCreatedDate() != null)	createSqlQuery.append("and  created_date = "+dto.getCreatedDate()+" ");

            if(page.getPageNumber()==0){
                createSqlQuery.append("order by company_name limit "+page.getPageSize()+" offset "+page.getPageNumber());
            }else{
                createSqlQuery.append("order by company_name limit "+page.getPageSize()+" offset "+(page.getPageSize()*page.getPageNumber()));
            }

            List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Company.class).getResultList();

            dtos = mapper.map(list,SingleCompanyDto[].class );

        }
        List<SingleCompanyDto> dtosList = Arrays.asList(dtos);

//        int start = Math.min((int) page.getOffset(), dtosList.size());
//        int end = Math.min((start + page.getPageSize()), dtosList.size());

        int start=0;
        int end=dtosList.size();
        int totalCount=0;


        if(user.getRole() == Role.ADMIN){
            createSqlQuery = new StringBuilder("select count(*) from company where status=1 ");
            if(dto.getCompanyName() != null)    createSqlQuery.append("and company_name ILIKE  '%"+dto.getCompanyName()+"%' ");
            if(dto.getCountryId() != null){
                Optional<Country> optCountry = countryRepository.findById(dto.getCountryId());
                if(!optCountry.isPresent()) {
                    log.info("Seçili Ülke Kaydı Bulunamadı..");
                }else{
                    createSqlQuery.append("and countryid = "+dto.getCountryId()+" ");
                }
            }
            if(dto.getCity() != null)			createSqlQuery.append("and  city ILIKE  '%"+dto.getCity()+"%' ");

            if(dto.getUserId() != null)			createSqlQuery.append("and  userid = "+dto.getUserId()+" ");

            if(dto.getCreatedDate() != null)	createSqlQuery.append("and  created_date = "+dto.getCreatedDate()+" ");


        } else if(user.getRole() == Role.EXPORTER){
            createSqlQuery = new StringBuilder("select count(*) from company where status=1 and userid ="+user.getUserId()+" ");
            if(dto.getCompanyName() != null)    createSqlQuery.append("and company_name ILIKE  '%"+dto.getCompanyName()+"%' ");
            if(dto.getCountryId() != null){
                Optional<Country> optCountry = countryRepository.findById(dto.getCountryId());
                if(!optCountry.isPresent()) {
                    log.info("Seçili Ülke Kaydı Bulunamadı..");
                }else{
                    createSqlQuery.append("and countryid = "+dto.getCountryId()+" ");
                }
            }
            if(dto.getCity() != null)			createSqlQuery.append("and  city ILIKE  '%"+dto.getCity()+"%' ");

            if(dto.getCreatedDate() != null)	createSqlQuery.append("and  created_date = "+dto.getCreatedDate()+" ");


        }

        List<Object> countList = entityManager.createNativeQuery(createSqlQuery.toString()).getResultList();
        for(Object data :countList){
            totalCount= Integer.valueOf(String.valueOf((BigInteger) data));
        }

        pageList = new PageImpl<>(dtosList.subList(start, end), page, totalCount);
        return pageList;
    }

    @Override
    public CompanyDto findById( Long id) throws NotFoundException {
//        String username = controlService.getUsernameFromToken(authHeader);
//        Optional<User> optUser = userRepository.findByUsername(username);
//        if(!optUser.isPresent()) {
//            throw new NotFoundException("Not found User");
//        }
        Optional<Company> optCompany = companyRepository.findById(id);
        if(!optCompany.isPresent()) {
            log.info("Şirket Kaydı Bulunamadı..");
            throw new NotFoundException("Şirket Kaydı Bulunamadı..");
        }
        CompanyDto dto = mapper.map(optCompany.get(),CompanyDto.class);
        return dto;
    }

    @Override
    public Boolean update(String authHeader, Long companyId, @Valid CompanyDto dto) throws Exception {
        if(companyId != dto.getCompanyId())
            return false;
//        String username = controlService.getUsernameFromToken(authHeader);
//        Optional<User> optUser = userRepository.findByUsername(username);
//        if(!optUser.isPresent()) {
//            throw new NotFoundException("Not found User");
//        }


        User user = controlService.getUserFromToken(authHeader);
        Optional<Country> optCountry = countryRepository.findById(dto.getCountryId());
        if(!optCountry.isPresent()) {
            log.info("Ülke Kaydı Bulunamadı..");
            return false;
        }
        Optional<Company> optCompany = companyRepository.findById(companyId);
        if(!optCompany.isPresent()){
            log.info("Şirket Kaydı Bulunamadı..");
            return false;
        }
        Company company = optCompany.get();

        /* Cari Kart Kullanıyor */
        String checkingCardName=company.getCompanyName();

        company.setCountry(optCountry.get());
        company.setAddress(dto.getAddress());
        company.setCity(dto.getCity());
        company.setUser(user);
        company.setTaxNo(dto.getTaxNo());
        company.setEmailAddress(dto.getEmailAddress());
        company.setCompanyName(dto.getCompanyName());
        company.setCompanyFax(dto.getCompanyFax());
        company.setCompanyPhone(dto.getCompanyPhone());
        company.setCompanyMobilePhone(dto.getCompanyMobilePhone());
        company = companyRepository.save(company);

        CheckingCard checkingCard = null;
        /* Cari Kart Güncelleme Başladı */
        Optional<CheckingCard> optCheckingCard = checkingCardRepository.findByCheckingCardName(checkingCardName);
        if(!optCheckingCard.isPresent()){
            checkingCard= new CheckingCard();
//            throw new Exception("Şirkete Ait Cari Kart Kaydı Bulunamadı");
        }else{

            checkingCard=optCheckingCard.get();
        }
        checkingCard.setCheckingCardName(company.getCompanyName());
        checkingCard.setCountry(company.getCountry());
        checkingCard.setCity(company.getCity());
        checkingCard.setAddress(company.getAddress());
        checkingCard.setEmail(company.getEmailAddress());
        checkingCard.setPhoneNumber(company.getCompanyPhone());
        checkingCard.setFaxNumber(company.getCompanyFax());
        if(company.getTaxNo()!=null && company.getTaxNo().trim().length()>0) {
            checkingCard.setTaxIdentificationNumber(Long.valueOf(company.getTaxNo().trim()));
        }
        checkingCard.setType(CheckingCardType.CUSTOMER);
        checkingCard.setSalesRepresentative(String.valueOf(company.getUser().getUserId()));
        checkingCard.setCompanyId(company.getCompanyId());
        checkingCard.setUser(user);
        checkingCard.setCreatedAt(new Date());
        checkingCard=checkingCardRepository.save(checkingCard);
        /* Cari Kart Güncelleme Bitti */

        return true;
    }

}
