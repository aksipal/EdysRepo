package com.via.ecza.repo;

import com.via.ecza.entity.CustomerOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerOrderStatusRepository extends JpaRepository<CustomerOrderStatus, Long> {

}
