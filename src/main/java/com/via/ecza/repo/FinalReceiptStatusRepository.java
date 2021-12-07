package com.via.ecza.repo;

import com.via.ecza.entity.FinalReceiptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FinalReceiptStatusRepository extends JpaRepository<FinalReceiptStatus, Long> {

    String SQL1 = "select * from final_receipt_status frs where frs.final_receipt_status_id>1 and frs.final_receipt_status_id<40";
    @Query(value = SQL1,nativeQuery = true)
    List<FinalReceiptStatus> getFinalReceiptStatus();
}
