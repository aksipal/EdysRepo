package com.via.ecza.repo;

import com.via.ecza.entity.RefundReceiptContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefundReceiptContentRepository extends JpaRepository<RefundReceiptContent, Long> {

}
