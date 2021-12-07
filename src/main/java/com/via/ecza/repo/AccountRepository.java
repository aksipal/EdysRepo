package com.via.ecza.repo;

import com.via.ecza.entity.Account;
import com.via.ecza.entity.CheckingCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByCheckingCardOrderByAccountIdAsc(CheckingCard checkingCard);

    String SQL1 = "select * from account a where a.account_type =:accountType and a.checking_card_id =:checkingCardId ";
    @Query(value = SQL1,nativeQuery = true)
    List<Account> getChequeAccounts(@Param("accountType") String accountType, @Param("checkingCardId") Long checkingCardId);
}
