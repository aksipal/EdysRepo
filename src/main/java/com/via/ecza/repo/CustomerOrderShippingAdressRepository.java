package com.via.ecza.repo;

import com.via.ecza.entity.Box;
import com.via.ecza.entity.CustomerOrder;
import com.via.ecza.entity.CustomerOrderShippingAdress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerOrderShippingAdressRepository extends JpaRepository<CustomerOrderShippingAdress, Long> {
    Optional<CustomerOrderShippingAdress> findByCustomerOrder(CustomerOrder customerOrder);
}
