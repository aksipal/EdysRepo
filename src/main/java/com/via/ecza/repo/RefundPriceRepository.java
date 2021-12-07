package com.via.ecza.repo;

import com.via.ecza.entity.RefundPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefundPriceRepository extends JpaRepository<RefundPrice, Long> {
}
