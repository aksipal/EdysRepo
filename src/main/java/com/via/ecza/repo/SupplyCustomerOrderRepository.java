package com.via.ecza.repo;

import com.via.ecza.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplyCustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

   // List<CustomerOrder> findByCustomerOrderDrugs(List<CustomerOrderDrugs>  customerOrderDrugs);
   // Optional<CustomerSupplyOrder> findById(Optional<CustomerSupplyOrder> customerSupplyOrder);


    ///Status kontrol edileceği zaman buna dön//Sipariş iptal durumu ?
    String SQL1 = "select * from customer_order co where co.customer_order_id =:customerOrderId and co.order_status_id>0";
    @Query(value = SQL1,nativeQuery = true)
    Optional<CustomerOrder> findByCustomerId(@Param("customerOrderId") Long customerOrderId);

}
