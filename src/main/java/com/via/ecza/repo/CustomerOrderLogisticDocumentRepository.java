package com.via.ecza.repo;

import com.via.ecza.entity.CustomerOrder;
import com.via.ecza.entity.CustomerOrderDrugs;
import com.via.ecza.entity.CustomerOrderLogisticDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerOrderLogisticDocumentRepository extends JpaRepository<CustomerOrderLogisticDocument, Long> {

    List<CustomerOrderLogisticDocument> findByCustomerOrder(CustomerOrder customerOrder);
}
