package com.via.ecza.repo;

import com.via.ecza.entity.Refund;
import com.via.ecza.entity.RefundOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundOfferRepository extends JpaRepository<RefundOffer, Long> {
    String SQL1 = "select * from refund_offer ro where ro.supplier_id=:supplier and ro.refund_offer_status_id=10 ";
    @Query(value = SQL1,nativeQuery = true)
    List<RefundOffer> getRefundOffersBySupplier(@Param("supplier") Long supplier);

}
