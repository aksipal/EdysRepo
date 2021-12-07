package com.via.ecza.repo;

import com.via.ecza.entity.Refund;
import com.via.ecza.entity.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundStatusRepository extends JpaRepository<RefundStatus,Long> {

    String SQL1 = "select * from refund_status r ";
    @Query(value = SQL1,nativeQuery = true)
    List<RefundStatus> getAllRefundStatus();
}
