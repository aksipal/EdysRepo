package com.via.ecza.service;

import com.via.ecza.dto.OtherCompanyDto;
import com.via.ecza.entity.OtherCompany;
import com.via.ecza.repo.OtherCompanyRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
@Transactional

public class OtherCompanyService {

    @Autowired
    private OtherCompanyRepository otherCompanyRepository;
    @Autowired
    private ModelMapper mapper;


    public List<OtherCompanyDto> findAllOtherCompanies() {

        List<OtherCompany> otherCompanies = otherCompanyRepository.findAll();
        OtherCompanyDto[] array = mapper.map(otherCompanies, OtherCompanyDto[].class);
        List<OtherCompanyDto> dtos = Arrays.asList(array);
        return dtos;
    }

    public OtherCompany findById(Long otherCompanyId) throws Exception {
        Optional<OtherCompany> optOtherCompany = otherCompanyRepository.findById(otherCompanyId);
        if (!optOtherCompany.isPresent()) {
            throw new Exception("Firma Bİlgisi Bulunamadı !");
        }
        return optOtherCompany.get();
    }
}
