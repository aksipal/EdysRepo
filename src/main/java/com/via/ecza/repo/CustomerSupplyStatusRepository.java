package com.via.ecza.repo;

import com.via.ecza.entity.CustomerSupplyStatus;
import com.via.ecza.entity.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerSupplyStatusRepository extends JpaRepository<CustomerSupplyStatus,Long> {

    String SQL1 = "select * from customer_supply_status r ";
    @Query(value = SQL1,nativeQuery = true)
    List<CustomerSupplyStatus> getAllOrderStatus();
}
