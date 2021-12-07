package com.via.ecza.repo;

import com.via.ecza.entity.PurchaseStatus;
import com.via.ecza.entity.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface PurchaseStatusRepository extends JpaRepository<PurchaseStatus,Long> {

    String SQL1 = "select * from purchase_status ps ";
    @Query(value = SQL1,nativeQuery = true)
    List<PurchaseStatus> getAllPurchaseStatus();
}
