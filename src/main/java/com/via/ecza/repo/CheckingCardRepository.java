package com.via.ecza.repo;

import com.via.ecza.entity.CheckingCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CheckingCardRepository extends JpaRepository <CheckingCard, Long> {

    Optional<CheckingCard> findByCheckingCardName(String checkingCardName);

    Optional<CheckingCard> findBySupplierId(Long supplierId);

    String SQL1 = "select * from checking_card cc order by cc.checking_card_name";
    @Query(value = SQL1,nativeQuery = true)
    List<CheckingCard> getAllCheckingCards();

}
