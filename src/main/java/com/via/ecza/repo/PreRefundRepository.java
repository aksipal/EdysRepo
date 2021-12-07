package com.via.ecza.repo;

import com.via.ecza.entity.PreRefund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreRefundRepository extends JpaRepository<PreRefund, Long> {

    String SQL1 = "select * from pre_refund pr join refund r on pr.refund_id = r.refund_id \n" +
            "where r.refund_id = :refundId and pr.pre_refund_status_id !=1 order by pr.drug_expiration_date desc";
    @Query(value = SQL1,nativeQuery = true)
    List<PreRefund> getAllWithRefundId(@Param("refundId") Long refundId);

    String SQL2 = "delete from pre_refund pr where pr.refund_id = :refundId and (pr.pre_refund_status_id =5 or pr.pre_refund_status_id =6)";
    @Modifying
    @Query(value = SQL2,nativeQuery = true)
    int deletePreRefundStatus5_6(@Param("refundId") Long refundId);

}
