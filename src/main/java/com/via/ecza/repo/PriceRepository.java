package com.via.ecza.repo;

import com.via.ecza.entity.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PriceRepository extends JpaRepository <Price, Long> {

    Optional<Price> findByDrugBarcode(Long drugBarcode);



}
