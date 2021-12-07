package com.via.ecza.repo;

import com.via.ecza.entity.RefundOfferStatus;
import com.via.ecza.entity.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundOfferStatusRepository extends JpaRepository<RefundOfferStatus,Long> {
    String SQL1 = "select * from refund_offer_status r ";
    @Query(value = SQL1,nativeQuery = true)
    List<RefundOfferStatus> getAllRefundOfferStatus();
}
