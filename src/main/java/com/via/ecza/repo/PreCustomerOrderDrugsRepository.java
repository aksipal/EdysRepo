package com.via.ecza.repo;

import com.via.ecza.entity.CustomerOrder;
import com.via.ecza.entity.PreCustomerOrderDrugs;
import com.via.ecza.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PreCustomerOrderDrugsRepository extends JpaRepository<PreCustomerOrderDrugs, Long> {

    Optional<PreCustomerOrderDrugs> findByPreCustomerOrderDrugIdAndCustomerOrder(
            Long preCustomerOrderDrugId, CustomerOrder customerOrder);

    Optional<PreCustomerOrderDrugs> findByPreCustomerOrderDrugIdAndCustomerOrderAndUser(
            Long preCustomerOrderDrugId, CustomerOrder customerOrder, User user);

    List<PreCustomerOrderDrugs> findByCustomerOrder(CustomerOrder customerOrder);
}
