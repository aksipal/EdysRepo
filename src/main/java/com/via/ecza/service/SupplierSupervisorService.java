package com.via.ecza.service;


import com.via.ecza.dto.SingleSupplierDto;
import com.via.ecza.dto.SingleSupplierSupervisorDto;
import com.via.ecza.dto.SupervisorDto;
import com.via.ecza.entity.Supplier;
import com.via.ecza.entity.SupplierSupervisor;
import com.via.ecza.repo.SupplierRepository;
import com.via.ecza.repo.SupplierSupervisorRepository;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SupplierSupervisorService {

    @Autowired
    private final SupplierRepository supplierRepository;
    @Autowired
    private final SupplierSupervisorRepository supplierSupervisorRepository;
    @Autowired
    private ModelMapper mapper;

    public Boolean save(@Valid SingleSupplierSupervisorDto dto) throws NotFoundException {
        Optional<Supplier> optionalSupplier = supplierRepository.findSupervisorsBySupplierId(dto.getSupplierId());
        if (!optionalSupplier.isPresent()) {
            throw new NotFoundException("Boyle bir Tedarikçi yok");
        }
        SingleSupplierDto singleSupplierDto = mapper.map(optionalSupplier.get(), SingleSupplierDto.class);
        singleSupplierDto.getSupplierSupervisors().removeIf(c -> (c.getStatus() == 0));
        if (singleSupplierDto.getSupplierSupervisors().size() >= 1) {
            return false;
        }

        SupplierSupervisor supplierSupervisor = mapper.map(dto, SupplierSupervisor.class);
        supplierSupervisor.setStatus(1);

        Supplier supplier = optionalSupplier.get();
        supplier.getSupplierSupervisors().add(supplierSupervisor);
        supplierSupervisor.setSupplier(supplier);
        supplierSupervisor = supplierSupervisorRepository.save(supplierSupervisor);
        supplier.setStatus(1);
        supplier = supplierRepository.save(supplier);
        return true;
    }


    public List<SupervisorDto> getAll() {
        List<SupplierSupervisor> list = supplierSupervisorRepository.findAll();
        SupervisorDto[] array = mapper.map(list, SupervisorDto[].class);
        List<SupervisorDto> dtoList = Arrays.asList(array);
        return dtoList;
    }


    public SupervisorDto findById(Long supervisorId) throws NotFoundException {
        Optional<SupplierSupervisor> optionalSupervisor = supplierSupervisorRepository.findById(supervisorId);
        if (!optionalSupervisor.isPresent()) {
            throw new NotFoundException("Boyle bir Supervisor yok");
        }
        SupervisorDto supervisorDto = mapper.map(optionalSupervisor.get(), SupervisorDto.class);
        System.out.println(supervisorDto);
        return supervisorDto;
    }


    public List<SupervisorDto> findBySupplierId(Long supplierId) {

        List<SupplierSupervisor> list = supplierSupervisorRepository.findBySupplierId(supplierId);
        SupervisorDto[] array = mapper.map(list, SupervisorDto[].class);
        List<SupervisorDto> dtoList = Arrays.asList(array);
        return dtoList;
    }


    public Boolean deleteBySupervisorId(Long supervisorId) throws NotFoundException {
        try {
            Optional<SupplierSupervisor> optionalSupervisor = supplierSupervisorRepository.findById(supervisorId);

            if (!optionalSupervisor.isPresent()) {
                return false;
            }
            Supplier supplier = optionalSupervisor.get().getSupplier();
            supplier.setStatus(2);
            SupplierSupervisor supplierSupervisor = optionalSupervisor.get();
            supplierSupervisor.setStatus(0);
            supplierSupervisor = supplierSupervisorRepository.save(supplierSupervisor);
            supplier = supplierRepository.save(supplier);
            return true;
        } catch (Exception e) {
            throw new NotFoundException(e + "Boyle bir Supervisor yok");
        }
    }


    public List<SupervisorDto> deleteBySupplierId(Long supplierId) {
        List<SupplierSupervisor> list = supplierSupervisorRepository.findBySupplierId(supplierId);
        SupervisorDto[] array = mapper.map(list, SupervisorDto[].class);
        List<SupervisorDto> dtoList = Arrays.asList(array);
        return dtoList;
    }


}

