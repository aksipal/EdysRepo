package com.via.ecza.manager.service;

import com.via.ecza.dto.CompanyDto;
import com.via.ecza.dto.SearchCompanyDto;
import com.via.ecza.dto.SingleCompanyDto;
import com.via.ecza.entity.User;
import javassist.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.validation.Valid;
import java.util.List;

public interface CompanyService {
    Boolean save(String authHeader, @Valid CompanyDto dto) throws Exception;
    List<SingleCompanyDto> getAll(String authHeader ) throws NotFoundException;
    List<CompanyDto> findBySearching(String authHeader,String companyName) throws NotFoundException;
    Page<SingleCompanyDto> getAllWithPagination(String authHeader, Pageable page, SearchCompanyDto dto) throws Exception;
    CompanyDto findById( Long id) throws NotFoundException;
    Boolean update(String authHeader, Long companyId, @Valid CompanyDto dto) throws Exception;


}
