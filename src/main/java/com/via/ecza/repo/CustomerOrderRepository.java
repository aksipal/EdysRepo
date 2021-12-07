package com.via.ecza.repo;

import com.via.ecza.entity.Customer;
import com.via.ecza.entity.CustomerOrder;
import com.via.ecza.entity.CustomerOrderStatus;
import com.via.ecza.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    List<CustomerOrder> findByStatus(int status);
    // customerOrderId customer
    Optional<CustomerOrder> findByCustomerOrderIdAndCustomer(long customerOrderId, Customer customer);

    String SQL1 = "select * from customer_order co  " +
            "join customer c ON c.customer_id = co.customer_id " +
            "where co.customer_order_id = :customerOrderId and " +
            "c.customer_id =:customerId " ;
    @Query(value = SQL1,nativeQuery = true)
    Optional<CustomerOrder> getSingleCustomerOrder(@Param("customerOrderId") Long customerOrderId, @Param("customerId") Long customerId);



    String SQL2 = "select * from customer_order co  " +
            "join customer c ON c.customer_id = co.customer_id " +
            "where co.customer_order_id = :customerOrderId and " +
            "c.customer_id =:customerId and user_id=:userId " ;
    @Query(value = SQL2,nativeQuery = true)
    Optional<CustomerOrder> getSingleCustomerOrderForExporter(@Param("customerOrderId") Long customerOrderId, @Param("customerId") Long customerId, @Param("userId") Long userId);

    Optional<CustomerOrder> findByCustomerOrderIdAndUser(Long customerOrderId, User user);

    Optional<CustomerOrder> findByCustomerOrderNo(String customerOrderNo);

    List<CustomerOrder> findByStatusAndOrderStatusOrderByCustomerOrderId(int status,CustomerOrderStatus orderStatus);
    Optional<CustomerOrder> findByStatusAndOrderStatusAndCustomerOrderId(int status,CustomerOrderStatus orderStatus, Long customerOrderId);

    Page<CustomerOrder> findByCustomerOrderNoAndOrderStatusAndStatus(String customerOrderNo,CustomerOrderStatus customerOrderStatus, int status,Pageable page);
    Page<CustomerOrder> findByOrderStatusAndStatus(CustomerOrderStatus customerOrderStatus, int status,Pageable page);


    String SQL3 = "select * from customer_order co where co.order_status_id in (30,40,50,60) and co.status=1 order by co.customer_order_id";
    @Query(value = SQL3,nativeQuery = true)
    Page<CustomerOrder> getAllCustomerOrderForPackaging( Pageable page);

    String SQL4 = "select * from customer_order co where co.order_status_id in (30,40,50,60) and co.customer_order_id=:customerOrderId and co.status=1";
    @Query(value = SQL4,nativeQuery = true)
    Optional<CustomerOrder> getSingleCustomerOrderForPackaging(@Param("customerOrderId") Long customerOrderId);

    String SQL5 = "select * from customer_order co where co.order_status_id in (30,40,50,60) " +
            "and co.customer_order_no ILIKE %:customerOrderNo% order by co.customer_order_id";
    @Query(value = SQL5,nativeQuery = true)
    Page<CustomerOrder> getPageableCustomerOrderForPackaging(@Param("customerOrderNo") String customerOrderNo, Pageable page);

    // Paketlenen ve paketlenmeyen sipariş ilaçlarının grafiği için (admin)
    String SQL6 = "select co.customer_order_id , sum(cod.charged_quantity) as charged_quantity, sum(cod.incomplete_quantity) as incomplete_quantity , " +
            "sum(cod.total_quantity)  as total_quantity from customer_order_drugs cod " +
            "join customer_order co on co.customer_order_id = cod.customer_order_id where co.customer_order_id =:customerOrderId group by  co.customer_order_id";
    @Query(value = SQL6,nativeQuery = true)
    Optional<Object> getTotalPackagingDataForAdmın(@Param("customerOrderId") Long customerOrderId);


    // Paketlenen ve paketlenmeyen sipariş ilaçlarının grafiği için (ihracatcı)
    String SQL7 = "select co.customer_order_id , sum(cod.charged_quantity) as charged_quantity, sum(cod.incomplete_quantity) as incomplete_quantity , " +
            "sum(cod.total_quantity)  as total_quantity from customer_order_drugs cod " +
            "join customer_order co on co.customer_order_id = cod.customer_order_id where co.customer_order_id =:customerOrderId and co.user_id =:userId group by  co.customer_order_id";
    @Query(value = SQL7,nativeQuery = true)
    Optional<Object> getTotalPackagingDataForExporter(@Param("customerOrderId") Long customerOrderId, @Param("userId") Long userId);

    String SQL8 = "select * from customer_order co  " +
            "join customer c ON c.customer_id = co.customer_id " +
            "where co.customer_order_id = :customerOrderId and " +
            "c.customer_id =:customerId and co.user_id=:userId " ;
    @Query(value = SQL8,nativeQuery = true)
    Optional<CustomerOrder> getSingleCustomerOrderExporter(@Param("customerOrderId") Long customerOrderId, @Param("customerId") Long customerId, @Param("userId") Long userId);


    String SQL9 = "select * from customer_order co  where co.customer_order_id = :customerOrderId and   user_id=:userId " ;
    @Query(value = SQL9,nativeQuery = true)
    Optional<CustomerOrder> getSingleCustomerOrderForExporter(@Param("customerOrderId") Long customerOrderId,@Param("userId") Long userId);
    

}
