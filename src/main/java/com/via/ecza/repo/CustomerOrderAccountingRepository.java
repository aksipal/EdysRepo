package com.via.ecza.repo;

import com.via.ecza.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerOrderAccountingRepository extends JpaRepository<CustomerOrder, Long> {
}
