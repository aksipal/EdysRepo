package com.via.ecza.repo;

import com.via.ecza.entity.Customer;
import com.via.ecza.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface CustomerRepository  extends JpaRepository<Customer, Long> {

    Page<Customer> findByUserAndStatus(User user, int status, Pageable page);
    Page<Customer> findByStatus(int status, Pageable page);

    List<Customer> findByUser(User user);


    String SQL1 = "select * from customer c where c.customer_id =:customerId and c.userid =:userid";
    @Query(value = SQL1,nativeQuery = true)
    Customer findByCustomerIdandUserid(@Param("customerId") Long customerId, @Param("userid") Long userid);

    String S1QL2 = "select * from customer where name = :name and surname = :surname and created_date = :created_date and countryid = :countryid  and companyid = :companyid  and city = :city";
    @Query(value = S1QL2,nativeQuery = true)
    List<Customer> searchCustomers(@Param("name") String name, @Param("surname") String surname, @Param("created_date") Date created_date, @Param("countryid") Long countryid, @Param("companyid") Long companyid, @Param("city") String city);

}
