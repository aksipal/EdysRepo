package com.via.ecza.repo;

import com.via.ecza.entity.CustomerReceipt;
import com.via.ecza.entity.DrugCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CustomerReceiptRepository extends JpaRepository<CustomerReceipt, Long> {

    String SQL1 = "select cod.drug_card_id from customer_receipt cr " +
            "join customer_order_drugs cod on cod.customer_receipt_id = cr.customer_receipt_id " +
            "where cr.invoice_id =:invoice_id";
    @Query(value = SQL1,nativeQuery = true)
    Long findCustomerReceiptForContent(@Param("invoice_id") Long invoiceId);
}
