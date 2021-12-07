package com.via.ecza.service;

import com.via.ecza.dto.*;
import com.via.ecza.entity.*;
import com.via.ecza.repo.AccountRepository;
import com.via.ecza.repo.CategoryRepository;
import com.via.ecza.repo.CheckingCardRepository;
import com.via.ecza.repo.CountryRepository;
import javassist.NotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AccountService {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ControlService controlService;
    @Autowired
    private CheckingCardRepository checkingCardRepository;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    public Boolean save(String authHeader, AccountSaveDto dto) throws Exception {
        try {
            Optional<CheckingCard> checkingCard = checkingCardRepository.findById(dto.getCheckingCardId());
            if(!checkingCard.isPresent())   throw new NotFoundException("Cari Kart Bulunamadı !");

            Optional<Country> country = countryRepository.findById(dto.getCountryId());
            if(!country.isPresent())        throw new NotFoundException("Ülke Bulunamadı !");





            User user = controlService.getUserFromToken(authHeader);
            Account account = mapper.map(dto, Account.class);
            account.setCheckingCard(checkingCard.get());
            account.setCountry(country.get());
            account.setUser(user);
            account.setCreatedAt(new Date());
            account.setStatus(1);

            if(dto.getCategoryId()!=null){
                Optional<Category> optionalCategory = categoryRepository.findById(dto.getCategoryId());
                if(!optionalCategory.isPresent()){
                    throw new NotFoundException("Muhasebe Kodu Bulunamadı !");
                }else{
                    account.setCategory(optionalCategory.get());
                }
            }

            account = accountRepository.save(account);

            return true;
        } catch (Exception e) {
            throw e;
        }
    }


    public List<AccountDto> getCheckingCardAccount(Long checkingCardId) throws  Exception {
        List<AccountDto> dtoList = null;

        Optional<CheckingCard> checkingCard = checkingCardRepository.findById(checkingCardId);
        if(!checkingCard.isPresent())   throw new NotFoundException("Cari Kart Bulunamadı !");

        List<Account> list = accountRepository.findByCheckingCardOrderByAccountIdAsc(checkingCard.get());

        AccountDto[] dtos = mapper.map(list, AccountDto[].class);
        dtoList = Arrays.asList(dtos);

        return dtoList;
    }

    public AccountDto getCheckingCardAccountDetail(Long accountId) throws Exception {
        Optional<Account> account = accountRepository.findById(accountId);
        if (!account.isPresent()) {
            throw new Exception("Hesap Bulunamadı !");
        }
        AccountDto dto = mapper.map(account.get(), AccountDto.class);
        return dto;
    }

    public Boolean updateAccount(String authHeader, AccountUpdateDto dto) throws Exception {
        try {

            User user = controlService.getUserFromToken(authHeader);

            Optional<Account> optionalAccount = accountRepository.findById(dto.getAccountId());
            if (!optionalAccount.isPresent()) {
                throw new Exception("Hesap Bulunamadı !");
            }

            Optional<CheckingCard> checkingCard = checkingCardRepository.findById(optionalAccount.get().getCheckingCard().getCheckingCardId());
            if(!checkingCard.isPresent())   throw new NotFoundException("Cari Kart Bulunamadı !");


            Account account = optionalAccount.get();
            account = mapper.map(dto, Account.class);
            account.setCheckingCard(checkingCard.get());

            if (dto.getCountryId() != null) {
                account.setCountry(countryRepository.findById(dto.getCountryId()).get());
            }
            if (user != null) {
                account.setUser(user);
            }
            account.setCreatedAt(new Date());

            if(dto.getCategoryId()!=null){
                Optional<Category> optionalCategory = categoryRepository.findById(dto.getCategoryId());
                if(!optionalCategory.isPresent()){
                    throw new NotFoundException("Muhasebe Kodu Bulunamadı !");
                }else{
                    account.setCategory(optionalCategory.get());
                }
            }

            accountRepository.save(account);
            return true;
        } catch (Exception e) {
            throw e;
        }
    }

    public Boolean deleteAccount(Long accountId) throws Exception{
        Optional<Account> optionalAccount = accountRepository.findById(accountId);
        if (!optionalAccount.isPresent()) {
            throw new Exception("Hesap Bulunamadı !");
        }

        Account account = optionalAccount.get();
        account.setStatus(0);
        accountRepository.save(account);
        return true;
    }

    public List<CheckingCardAccountDto> getAccounts(AccountTypesDto dto) throws Exception {

        List<Account> accountList = accountRepository.getChequeAccounts(dto.getAccountType().toString(), dto.getCheckingCardId());
        CheckingCardAccountDto[] dtos = mapper.map(accountList, CheckingCardAccountDto[].class);
        List<CheckingCardAccountDto> dtosList = Arrays.asList(dtos);

        return dtosList;
    }
}
