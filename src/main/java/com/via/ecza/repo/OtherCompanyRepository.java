package com.via.ecza.repo;

import com.via.ecza.entity.CheckingCard;
import com.via.ecza.entity.OtherCompany;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtherCompanyRepository extends JpaRepository<OtherCompany, Long> {

    Optional<OtherCompany> findByCheckingCard(CheckingCard checkingCard);

}
