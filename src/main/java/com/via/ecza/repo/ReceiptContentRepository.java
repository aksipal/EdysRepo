package com.via.ecza.repo;

import com.via.ecza.entity.ReceiptContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceiptContentRepository extends JpaRepository<ReceiptContent, Long> {
    String SQL1 = "delete from receipt_content where receipt_id = :receiptId";
    @Modifying
    @Query(value = SQL1,nativeQuery = true)
    int deleteReceiptContentToReceiptId(@Param("receiptId") Long receiptId);
}
