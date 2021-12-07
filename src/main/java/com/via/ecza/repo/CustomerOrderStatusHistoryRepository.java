package com.via.ecza.repo;

import com.via.ecza.entity.CustomerOrder;
import com.via.ecza.entity.CustomerOrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerOrderStatusHistoryRepository extends JpaRepository<CustomerOrderStatusHistory, Long> {

    List<CustomerOrderStatusHistory> findByCustomerOrder(CustomerOrder customerOrder);
}
