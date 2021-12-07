package com.via.ecza.repo;

import com.via.ecza.entity.Supplier;
import com.via.ecza.entity.SupplierSupervisor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    String SQL1 = "select * from supplier ss where ss.supplier_id =:supplierId and ss.status>0";
    @Query(value = SQL1,nativeQuery = true)
    Optional<Supplier> findSupervisorsBySupplierId(@Param("supplierId") Long supplierId);

    String SQL2 = "select * from supplier ss where ss.status>0";
    @Query(value = SQL2,nativeQuery = true)
    List<Supplier> getAll();

    String SQL3 = "select * from supplier ss where ss.userid =:userId ";
    @Query(value = SQL3,nativeQuery = true)
    Optional<Supplier> getSupplierByUser(@Param("userId") Long userId);

}
