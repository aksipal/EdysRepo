package com.via.ecza.repo;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.via.ecza.entity.SupplierSupervisor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface SupplierSupervisorRepository extends JpaRepository<SupplierSupervisor, Long> {

//CRUD

    String SQL1 = "select * from supplier_supervisor ss where ss.supplierid =:supplierId and ss.status>0";
    @Query(value = SQL1,nativeQuery = true)
    List<SupplierSupervisor> findBySupplierId(@Param("supplierId") Long supplierId);

    String SQL2 = "update supplier_supervisor set status =0 where supplier_id =:supplierId";
    @Query(value = SQL2,nativeQuery = true)
    Boolean setSupervisorsDisable(@Param("supplierId") Long supplierId);

}
