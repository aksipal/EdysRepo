package com.via.ecza.repo;

import com.via.ecza.entity.CustomerSupplyOrder;
import com.via.ecza.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Ref;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    String SQL1 = "select * from refund r where r.supplier_id=:supplier and r.refund_status_id>0 and r.refund_status_id<50 ";
    @Query(value = SQL1,nativeQuery = true)
    List<Refund> getRefundsBySupplier(@Param("supplier") Long supplier);

    String SQL2 = "select r.* from refund r where r.receipt_id=:receiptId ";
    @Query(value = SQL2,nativeQuery = true)
    List<Refund> getByRefundId(@Param("receiptId") Long receiptId);

    String SQL3 = "select r.* from refund r where r.refund_id=:refundId ";
    @Query(value = SQL3,nativeQuery = true)
    Optional<Refund> getByCustomerSupplyOrderSellId(@Param("refundId") Long refundId);

    String SQL4 = "select rf.* from refund rf " +
            "join receipt r on r.supplier_id =rf.supplier_id " +
            "where r.receipt_id =:receiptId and rf.receipt_id is null ";
    @Query(value = SQL4,nativeQuery = true)
    List<Refund> getByOrderRefund(@Param("receiptId") Long receiptId);


//    String SQL5 = "select * from refund r " +
//            "join receipt r2 on r2.receipt_id = r.receipt_id " +
//            "join final_receipt fr on fr.final_receipt_id = r2.final_receipt_id " +
//            "where fr.final_receipt_id =:finalReceiptId";
//    @Query(value = SQL5,nativeQuery = true)
//    List<Refund> getRefundsByFinalReceiptId(@Param("finalReceiptId") Long finalReceiptId);
}
