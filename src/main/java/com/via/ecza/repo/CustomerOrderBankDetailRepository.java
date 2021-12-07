package com.via.ecza.repo;

import com.via.ecza.entity.CustomerOrder;
import com.via.ecza.entity.CustomerOrderBankDetail;
import com.via.ecza.entity.CustomerOrderShippingAdress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerOrderBankDetailRepository extends JpaRepository<CustomerOrderBankDetail, Long> {
    Optional<CustomerOrderBankDetail> findByCustomerOrder(CustomerOrder customerOrder);
}
