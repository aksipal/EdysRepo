package com.via.ecza.service;


import com.via.ecza.dto.*;
import com.via.ecza.entity.*;
import com.via.ecza.entity.enumClass.CheckingCardType;
import com.via.ecza.entity.enumClass.Role;
import com.via.ecza.repo.*;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.EntityManager;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SupplierService {
    @Autowired
    private final SupplierRepository supplierRepository;
    @Autowired
    private final SupplierSupervisorRepository supplierSupervisorRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private CheckingCardRepository checkingCardRepository;
    @Autowired
    private CountryRepository countryRepository;

    public List<SingleSupplierDto> getAll() throws NotFoundException {

        List<Supplier> listsupplier = supplierRepository.getAll();

        SingleSupplierDto[] listSingleSupplierDto = mapper.map(listsupplier, SingleSupplierDto[].class);
        if (listSingleSupplierDto.length < 1) {
            throw new NotFoundException("Şirket Kaydı Bulunamadı..");
        }

        for (SingleSupplierDto singleSupplierDto : listSingleSupplierDto) {
            singleSupplierDto.getSupplierSupervisors().removeIf((c -> (c.getStatus() == 0)));
        }
        List<SingleSupplierDto> dtos = Arrays.asList(listSingleSupplierDto);
        return dtos;

    }


    public Page<SingleSupplierDto> search(SupplierSearchDto dto,Integer pageNo,Integer pageSize,String sortBy) throws NotFoundException {
        StringBuilder createSqlQuery = new StringBuilder("select * from supplier where status>0 and supplier_id>1");
        if (dto.getSupplierName() != null)
            createSqlQuery.append("and supplier_name ILIKE  '%" + dto.getSupplierName() + "%' ");

//        if (dto.getSupplierCity() != null)
//            createSqlQuery.append("and supplier_city ILIKE '%" + dto.getSupplierCity() + "%' ");
//
//        if (dto.getSupplierDistrict() != null)
//            createSqlQuery.append("and  supplier_district ILIKE '%" + dto.getSupplierDistrict() + "%' ");

        if (dto.getPhoneNumber() != null)
            createSqlQuery.append("and  phone_number ILIKE  '%" + dto.getPhoneNumber() + "%' ");

        if (dto.getSupplierProfit() != null)
            createSqlQuery.append("and supplier_profit<= '" + dto.getSupplierProfit() + "' ");

        createSqlQuery.append(" order by supplier_name");
        List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Supplier.class).getResultList();

        SingleSupplierDto[] dtos = mapper.map(list, SingleSupplierDto[].class);
        List<SingleSupplierDto> dtosList = Arrays.asList(dtos);
        //  EmailService emailService=new EmailService();

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
        int start = Math.min((int) paging.getOffset(), dtosList.size());
        int end = Math.min((start + paging.getPageSize()), dtosList.size());

        Page<SingleSupplierDto> pageList = new PageImpl<>(dtosList.subList(start, end), paging, dtosList.size());

        return pageList;


    }


    public Boolean deleteBySupplierId(Long supplierId) throws NotFoundException {
        try {
            Optional<Supplier> optionalSupplier = supplierRepository.findById(supplierId);
            if (!optionalSupplier.isPresent()) {
                return false;
            }

            Supplier supplier = optionalSupplier.get();
            supplier.setStatus(0);
            //SUpervisorları da status 0 la
            // supplier.getSupplierSupervisors().forEach();
            for (SupplierSupervisor supplierSupervisor : supplier.getSupplierSupervisors()) {
                supplierSupervisor.setStatus(0);
                supplierSupervisorRepository.save(supplierSupervisor);
            }

            //  supplier.getSupplierSupervisors().stream().filter(c-> (c.getStatus()==1));

            supplier = supplierRepository.save(supplier);
            return true;
        } catch (Exception e) {
            throw new NotFoundException(e + "Boyle bir Tedarikçi yok");
        }

    }

    public SingleSupplierDto findSupervisorsBySupplierId(Long supplierId) throws NotFoundException {
        Optional<Supplier> optionalSupplier = supplierRepository.findSupervisorsBySupplierId(supplierId);
        if (!optionalSupplier.isPresent()) {
            throw new NotFoundException("Boyle bir Tedarikçi yok");
        }
        SingleSupplierDto singleSupplierDto = mapper.map(optionalSupplier.get(), SingleSupplierDto.class);
        singleSupplierDto.getSupplierSupervisors().removeIf(c -> (c.getStatus() == 0));
        return singleSupplierDto;
    }

    public Boolean save(SupplierSaveDto dto) throws Exception {

        if (dto.getSupplierProfit() == null) {
            dto.setSupplierProfit(0F);
        }
        Supplier supplier = mapper.map(dto, Supplier.class);
        supplier.setStatus(1);


        //  SupplierSupervisor supplierSupervisor = mapper.map(dto, SupplierSupervisor.class);
        SupplierSupervisor supplierSupervisor = new SupplierSupervisor();
        supplierSupervisor.setStatus(1);
        supplierSupervisor.setName(dto.getName());
        supplierSupervisor.setSurname(dto.getSurname());
        supplierSupervisor.setEmail(dto.getEmail());
        supplierSupervisor.setJobTitle(dto.getJobTitle());
        supplierSupervisor.setPhoneNumber(dto.getSupervisorPhoneNumber());
        supplierSupervisor.setSupplier(supplier);

        //supplier.getSupplierSupervisors().add(supplierSupervisor);
        List<SupplierSupervisor> list = new ArrayList<>();
        list.add(supplierSupervisor);
        supplier.setSupplierSupervisors(list);


        try {
            supplier = supplierRepository.save(supplier);
            supplierSupervisor = supplierSupervisorRepository.save(supplierSupervisor);
        } catch (Exception e) {
            throw new NotFoundException("Hatalı Kayıt" + e);// gerekli olmadan devam edilebilinir
        }

        String userName = supplier.getSupplierName();
        userName = userName.replace(" ", "") + supplier.getSupplierId().toString();

        User user = new User();
        user.setName(supplier.getSupplierName());
        user.setSurname("Eczane");
        user.setUsername(userName);
        //user.setRealPassword(userName + "123");
        user.setPassword(passwordEncoder.encode(userName + "123"));
        user.setCreatedDate(new Date());
        user.setRole(Role.PHARMACY);
        userRepository.save(user);

        System.out.println(" USERNAME :" + userName);
        System.out.println(" USERPASSWRD :" + userName + "123");
        // Optional<User> us=userRepository.findById(1L);//Geçici olarak ekledik
        //supplier.setUser(us.get());//Geçici olarak ekledik

        supplier.setUser(user);
        supplier.setSupplierTaxNo(dto.getSupplierTaxNo());
        supplier = supplierRepository.save(supplier);


        /* Cari Kart Oluşturma Başladı */
        CheckingCard checkingCard = new CheckingCard();
        checkingCard.setCheckingCardName(supplier.getSupplierName());
        checkingCard.setCountry(countryRepository.findByName("Türkiye"));
        checkingCard.setCity(supplier.getSupplierCity());
        checkingCard.setAddress(supplier.getSupplierAddress());
        checkingCard.setEmail(supplier.getSupplierEmail());
        checkingCard.setPhoneNumber(supplier.getPhoneNumber());
        checkingCard.setFaxNumber(supplier.getSupplierFax());
        checkingCard.setType(CheckingCardType.SUPPLIER);
        checkingCard.setCreatedAt(new Date());
        checkingCard.setSupplierId(supplier.getSupplierId());
        checkingCard=checkingCardRepository.save(checkingCard);
        /* Cari Kart Oluşturma Bitti */


        return true;
    }

    public List<SingleSupplierDto> findBySearchingWithSupervisor(SupplierComboboxSearchDto dto) {

            StringBuilder createSqlQuery = new StringBuilder("select * from supplier where status=1 and supplier_name ILIKE '%" + dto.getSupplierName() + "%' ");

            List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Supplier.class).getResultList();
            SingleSupplierDto[] dtos = mapper.map(list, SingleSupplierDto[].class);
            List<SingleSupplierDto>  dtoList = Arrays.asList(dtos);
            return dtoList;

    }
    public List<SingleSupplierDto> findBySearching(SupplierComboboxSearchDto dto) {
        List<SingleSupplierDto> dtoList = new ArrayList<>();
        if (dto.getSupplierName().length() > 1) {
            StringBuilder createSqlQuery = new StringBuilder("select * from supplier where supplier_name ILIKE '%" + dto.getSupplierName() + "%' ");

            List<Object> list = entityManager.createNativeQuery(createSqlQuery.toString(), Supplier.class).getResultList();
            SingleSupplierDto[] dtos = mapper.map(list, SingleSupplierDto[].class);
            dtoList = Arrays.asList(dtos);
            return dtoList;
        } else {
            return dtoList;
        }
    }

}
