package com.via.ecza.service;


import com.via.ecza.dto.VersionInformationDto;
import com.via.ecza.dto.VersionInformationSearchDto;
import com.via.ecza.entity.VersionInformation;
import com.via.ecza.repo.VersionInformationRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VersionInformationService {


    @Autowired
    private VersionInformationRepository versionInformationRepository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private EntityManager entityManager;


    public List<VersionInformationDto> search() {

        Optional<VersionInformation> version = versionInformationRepository.findForLastRecord();

        List<VersionInformation> optList = versionInformationRepository.getVersionInformationList(version.get().getVersionNumber());
        if (optList.size()>0) {
            VersionInformationDto[] array = mapper.map(optList, VersionInformationDto[].class);
            List<VersionInformationDto> list = Arrays.asList(array);
            return list;
        }
        return null;
    }


}
