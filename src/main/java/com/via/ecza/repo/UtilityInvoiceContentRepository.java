package com.via.ecza.repo;


import com.via.ecza.entity.UtilityInvoiceContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UtilityInvoiceContentRepository  extends JpaRepository<UtilityInvoiceContent, Long> {

    //Hizmet Faturası Alış veya Satış Content Listesi
    String SQL1 = "select * from utility_invoice_content uic where uic.invoice_purpose=:invoicePurpose and uic.userid=:userId and uic.invoice_id is null and other_company_id=:otherCompanyId";
    @Query(value = SQL1,nativeQuery = true)
    List<UtilityInvoiceContent> getInvoiceContentList(@Param("userId") Long userId,@Param("invoicePurpose") String invoicePurpose,@Param("otherCompanyId") Long otherCompanyId);


}
