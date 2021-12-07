package com.via.ecza.repo;

import com.via.ecza.entity.Box;
import com.via.ecza.entity.CustomerOrderDrugs;
import com.via.ecza.entity.SmallBox;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SmallBoxRepository extends JpaRepository<SmallBox, Long> {

    Page<SmallBox> findBySmallBoxNoContaining(String smallBoxNo, Pageable page);

    Optional<SmallBox> findBySmallBoxNo(String smallBoxNo);

    String SQL1 = "select sb.* from small_box sb " +
            "join box_drug_list bdl on bdl.small_box_id = sb.small_box_id group by sb.small_box_id order by sb.small_box_id";
    @Query(value = SQL1,nativeQuery = true)
    Page<SmallBox> getAllWithBoxDrugListWithPage( Pageable page);

    List<SmallBox> findBySmallBoxNoContaining(String smallBoxNo);

    String SQL2 = "select count(dc.drug_card_id), dc.drug_name, dc.drug_code, d.expiration_date from small_box sb " +
            "join depot d on d.small_box_id = sb.small_box_id " +
            "join drug_card dc on dc.drug_card_id = d.drug_card_id " +
            "where sb.small_box_id =:smallBoxId group by dc.drug_card_id, d.expiration_date";
    @Query(value = SQL2,nativeQuery = true)
    List<Object[]> getSmallBoxCount(@Param("smallBoxId") Long smallBoxId);



    String SQL3 = "select count(d.drug_barcode), d.drug_barcode, min(d.expiration_date) from depot d " +
            "join small_box sb on sb.small_box_id = d.small_box_id " +
            "join drug_card dc on dc.drug_card_id = d.drug_card_id " +
            "where sb.small_box_id =:smallBoxId group by d.drug_barcode";
    @Query(value = SQL3,nativeQuery = true)
    List<Object[]> getCommunicationData(@Param("smallBoxId") Long smallBoxId);

    String SQL4 = "select * from small_box sb join customer_order co on co.customer_order_id = sb.customer_order_id " +
            "where sb.customer_order_id =:customerOrderId";
    @Query(value = SQL4,nativeQuery = true)
    List<SmallBox> findSmallBoxesForOutFromDepot(@Param("customerOrderId") Long customerOrderId);

}
