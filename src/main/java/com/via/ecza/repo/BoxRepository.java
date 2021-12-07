package com.via.ecza.repo;

import com.via.ecza.entity.Box;
import com.via.ecza.entity.CustomerOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoxRepository extends JpaRepository<Box, Long> {

    Page<Box> findByBoxNo (Pageable page,String boxNo);

    String SQL1 = "select * from box b join customer_order co on co.customer_order_id = b.customer_order_id  " +
            "where b.customer_order_id =:customerOrderId order by b.box_id";
    @Query(value = SQL1,nativeQuery = true)
    List<Box> findBoxes(@Param("customerOrderId") Long customerOrderId);

    Optional<Box> findByBoxIdAndCustomerOrder(Long boxId, CustomerOrder customerOrder);

    String SQL2 = "select d.drug_barcode, dc.drug_name, count(d.drug_barcode) as quantity, min(d.expiration_date) as expiration_date, dc.status as status,  " +
            " d.drug_card_id as drug_card_id from  box_drug_list b  join depot d on b.depot_id = d.depot_id join drug_card dc on d.drug_card_id = dc.drug_card_id " +
            " join box b2 on b2.box_id  = b.box_id  where b.box_id = :boxId group by d.drug_barcode, dc.drug_name, dc.status, d.drug_card_id  ";
    @Query(value = SQL2,nativeQuery = true)
    List<Object[]> getDrugNamesForBox(@Param("boxId") Long boxId);

    Optional<Box> findByBoxNo(String boxNo);


    String SQL3 =  " select count(dc.drug_card_id), dc.drug_name, dc.drug_code,  sb.small_box_no  from box b  " +
            "join depot d on d.box_id =b.box_id   " +
            "join drug_card dc on dc.drug_card_id = d.drug_card_id   " +
            "join small_box sb on sb.small_box_id = d.small_box_id " +
            "where b.box_id =:boxId  group by  dc.drug_card_id,  sb.small_box_no " +
            "UNION ALL " +
            "select count(dc.drug_card_id), dc.drug_name, dc.drug_code, '' as small_box_no from box b   " +
            "join depot d on d.box_id =b.box_id   " +
            "join drug_card dc on dc.drug_card_id = d.drug_card_id   " +
            "where b.box_id =:boxId and d.small_box_id is null group by  dc.drug_card_id ";
    @Query(value = SQL3,nativeQuery = true)
    List<Object[]> getBoxCount(@Param("boxId") Long boxId);

    String SQL4 = "select * from box b join customer_order co on co.customer_order_id = b.customer_order_id " +
            "where b.customer_order_id =:customerOrderId";
    @Query(value = SQL4,nativeQuery = true)
    List<Box> findBoxesForOutFromDepot(@Param("customerOrderId") Long customerOrderId);

}
