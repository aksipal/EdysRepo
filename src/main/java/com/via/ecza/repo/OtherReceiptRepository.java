package com.via.ecza.repo;

import com.via.ecza.entity.OtherReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OtherReceiptRepository extends JpaRepository<OtherReceipt, Long> {

    String SQL1 = "select * from other_receipt where previous_invoice_id=:previousInvoiceId";
    @Query(value = SQL1,nativeQuery = true)
    List<OtherReceipt> getOtherReceiptForPreviousInvoiceId(@Param("previousInvoiceId") Long previousInvoiceId);

}
