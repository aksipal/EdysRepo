package com.via.ecza.repo;

import com.via.ecza.entity.SupplyOrderPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplyOrderPriceRepository extends JpaRepository<SupplyOrderPrice,Long> {
}
