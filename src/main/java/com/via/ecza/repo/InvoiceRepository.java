package com.via.ecza.repo;

import com.via.ecza.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNo(String invoiceNo);

    String SQL1 = "select * from invoice where checking_card_id =:checkingCardId " +
            "and other_checking_card_id =:otherCheckingCardId " +
            "and total_price <> total_charge_price";

    @Query(value = SQL1, nativeQuery = true)
    List<Invoice> getAllInvoices(@Param("checkingCardId") Long checkingCardId,
                                 @Param("otherCheckingCardId") Long otherCheckingCardId);


    //KAPANMAMIŞ TİCARİ ALIŞ VE HİZMET ALIŞ FATURALARI GELECEK
    String SQL2 = "select * from invoice " +
            "where checking_card_id =:checkingCardId " +
            "and other_checking_card_id =:otherCheckingCardId " +
            "and invoice_purpose=:invoicePurpose " +
            "and invoice_status_id=10 " +
            "order by invoice_id";

    @Query(value = SQL2, nativeQuery = true)
    List<Invoice> getAllInvoicesToCheckingCardOrderById(@Param("checkingCardId") Long checkingCardId,
                                                        @Param("otherCheckingCardId") Long otherCheckingCardId,
                                                        @Param("invoicePurpose") String invoicePurpose);
}