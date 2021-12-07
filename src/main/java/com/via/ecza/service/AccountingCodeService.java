package com.via.ecza.service;

import com.via.ecza.dto.*;
import com.via.ecza.entity.AccountingCode;
import com.via.ecza.entity.Category;
import com.via.ecza.repo.AccountingCodeRepository;
import com.via.ecza.repo.CategoryRepository;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@Transactional
public class AccountingCodeService {

    @Autowired
    private AccountingCodeRepository accountingCodeRepository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private CategoryRepository categoryRepository;

    public Page<AccountingCodeDto> search(AccountingCodeSearchDto dto, Integer pageNo, Integer pageSize, String sortBy) {

        StringBuilder createSqlQuery = new StringBuilder("select * from accounting_code ac where ac.accounting_code_id >=1 ");

        if (dto.getCode() != null) createSqlQuery.append("and ac.code =  '%" + dto.getCode().trim() + "%' ");

        if (dto.getName() != null) createSqlQuery.append("and ac.name ILIKE '%"+dto.getName()+"%' " );

        createSqlQuery.append(" order by ac.code ASC");


        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), AccountingCode.class).getResultList();

        AccountingCodeDto[] dtos = mapper.map(list, AccountingCodeDto[].class);
        List<AccountingCodeDto> dtosList = Arrays.asList(dtos);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
        int start = Math.min((int) paging.getOffset(), dtosList.size());
        int end = Math.min((start + paging.getPageSize()), dtosList.size());

        Page<AccountingCodeDto> pageList = new PageImpl<>(dtosList.subList(start, end), paging, dtosList.size());

        return pageList;

    }
    public String save(@Valid AccountingCodeSaveDto dto) throws Exception {
        try {
            int control=-1;

            if(dto.getCategoryId()==0){
                control=accountingCodeRepository.findByNameAndNullCategory(dto.getName());
            }else{
                control=accountingCodeRepository.findByNameAndCategory(dto.getName(),(long)dto.getCategoryId());
            }


            if(control>0){
                return "Aynı Muhasebe Kodu Tekrar Eklenemez";
            }else{
                AccountingCode accountingCode=new AccountingCode();
                accountingCode.setCode(dto.getCode().trim());
                accountingCode.setName(dto.getName().trim());
                accountingCode.setCreatedDate(new Date());
                accountingCode.setStatus(1);
                accountingCode.setReverseWorkingAccount(dto.getReverseWorkingAccount());
                accountingCode.setCategory(categoryRepository.findById((long)dto.getCategoryId()).get());

                accountingCode=accountingCodeRepository.save(accountingCode);
                return "Kayıt Başarıyla Eklendi.";
            }

        } catch (Exception e) {
            throw e;
        }
    }


    public String update(@Valid AccountingCodeUpdateDto dto) throws NotFoundException {
        AccountingCode accountingCode=accountingCodeRepository.findById(dto.getAccountingCodeId()).get();
        if(accountingCode==null){
            throw new NotFoundException("Muhasebe Kodu Bulunamadı");
        }


        int control=-1;

        if(dto.getCategoryId()==0){
            control=accountingCodeRepository.findByNameAndNullCategory(dto.getName());
        }else{
            control=accountingCodeRepository.findByNameAndCategory(dto.getName(),(long)dto.getCategoryId());
        }


        if(control>1){
            return "Aynı Muhasebe Kodu Tekrar Eklenemez";
        }else{

            accountingCode.setCode(dto.getCode().trim());
            accountingCode.setName(dto.getName().trim());
            accountingCode.setStatus(1);
            accountingCode.setReverseWorkingAccount(dto.getReverseWorkingAccount());
            accountingCode.setCategory(categoryRepository.findById((long)dto.getCategoryId()).get());

            accountingCode=accountingCodeRepository.save(accountingCode);
            return "Güncelleme İşlemi Başarılı.";
        }

    }

    public List<AccountingCodeDto> getAllAccountingCodes() {

        List<AccountingCode> list= accountingCodeRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));

        AccountingCodeDto[] dtos = mapper.map(list, AccountingCodeDto[].class);
        List<AccountingCodeDto> dtosList = Arrays.asList(dtos);
        return dtosList;
    }
}
