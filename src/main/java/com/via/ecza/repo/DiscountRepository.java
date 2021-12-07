package com.via.ecza.repo;

import com.via.ecza.dto.CustomerSuppliersDto;
import com.via.ecza.entity.Customer;
import com.via.ecza.entity.Discount;
import com.via.ecza.entity.DrugCard;
import com.via.ecza.entity.SupplierSupervisor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository <Discount, Long> {

   // Optional<Discount> findByDrugCard(Long drugCard);

    String SQL1 = "select * from discount d where d.drug_card_id =:drugCard";
    @Query(value = SQL1,nativeQuery = true)
    Optional<Discount> findByDrugCard(@Param("drugCard") Long drugCard);


}
