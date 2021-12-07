package com.via.ecza.repo;

import com.via.ecza.entity.CustomerOrder;
import com.via.ecza.entity.CustomerSupplyOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportingRepository extends JpaRepository<CustomerOrder, Long> {

    String SQL1 = "select * from customer_order co where co.order_status_id = 14 and co.user_id =:userId " ;
    @Query(value = SQL1,nativeQuery = true)
    List<CustomerOrder[]> getSingleExporterOrder(@Param("userId") Long userId);

    String SQL2 = "select co.* from customer_order co where co.customer_order_id =:customerOrderId ";
    @Query(value = SQL2,nativeQuery = true)
    List<CustomerOrder> getByCustomerOrderId(@Param("customerOrderId") Long customerOrderId);
}
