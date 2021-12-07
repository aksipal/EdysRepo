package com.via.ecza.repo;

import com.via.ecza.entity.AccountActivity;
import com.via.ecza.entity.CheckingCard;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AccountActivityRepository extends JpaRepository<AccountActivity, Long> {
    //fatura id'sine göre hareket tablosu taranır ve fatura eden kişinin aldığı toplam tutar toplanır
    String SQL1 = "select sum(charge) from account_activity where invoice_id =:invoiceId";
    @Query(value = SQL1,nativeQuery = true)
    Double sumOfCharge(@Param("invoiceId") Long invoiceId);

    //fatura id'sine göre hareket tablosu taranır ve fatura edilen kişinin ödediği toplam tutar toplanır
    String SQL2 = "select sum(debt) from account_activity where invoice_id =:invoiceId";
    @Query(value = SQL2,nativeQuery = true)
    Double sumOfDebt(@Param("invoiceId") Long invoiceId);

    //2 cari arasındaki toplam borç bilgisi
    String SQL3 = "select sum(debt) debt from account_activity where checking_card_id=:checkingCardId and other_checking_card_id=:otherCheckingCardId";
    @Query(value = SQL3,nativeQuery = true)
    Double sumOfDebtToCheckingCards(@Param("checkingCardId") Long checkingCardId,
                                             @Param("otherCheckingCardId") Long otherCheckingCardId);

    //2 cari arasındaki toplam borç bilgisi
    String SQL4 = "select sum(charge) charge from account_activity where checking_card_id=:checkingCardId and other_checking_card_id=:otherCheckingCardId";
    @Query(value = SQL4,nativeQuery = true)
    Double sumOfChargeToCheckingCards(@Param("checkingCardId") Long checkingCardId,
                                    @Param("otherCheckingCardId") Long otherCheckingCardId);

    List<AccountActivity> findByCheckingCard(CheckingCard checkingCard, Pageable pageable);

}
