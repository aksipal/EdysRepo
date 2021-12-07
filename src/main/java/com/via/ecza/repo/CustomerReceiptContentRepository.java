package com.via.ecza.repo;

import com.via.ecza.entity.CustomerOrderDrugs;
import com.via.ecza.entity.CustomerReceiptContent;
import com.via.ecza.entity.DrugCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerReceiptContentRepository extends JpaRepository<CustomerReceiptContent, Long> {

    String SQL1 = "delete from customer_receipt_content crc where crc.customer_receipt_id is null " +
            "and crc.customer_order_id=:customerOrderId ";
    @Modifying
    @Query(value = SQL1,nativeQuery = true)
    int deleteCustomerReceiptContent(@Param("customerOrderId") Long customerOrderId);

    String SQL2 = "select * from customer_receipt_content crc where crc.drug_card_id =:drug_card_id " +
                "order by crc.customer_receipt_content_id desc limit 1";
    @Query(value = SQL2,nativeQuery = true)
    CustomerReceiptContent findCustomerReceiptContentForSetReceiptId(@Param("drug_card_id") Long drugCardId);

    String SQL3 = "select * from customer_receipt_content crc where crc.customer_order_id=:customerOrderId " +
            "and crc.customer_receipt_id is null";
    @Query(value = SQL3,nativeQuery = true)
    List<CustomerReceiptContent> findCustomerReceiptContentsForCustomerReceiptId(@Param("customerOrderId") Long customerOrderId);

}
