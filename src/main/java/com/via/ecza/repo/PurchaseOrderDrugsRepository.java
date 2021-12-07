package com.via.ecza.repo;

import com.via.ecza.entity.PurchaseOrderDrugs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderDrugsRepository extends JpaRepository<PurchaseOrderDrugs,Long> {

    String SQL1 = "select * from purchase_order_drugs so where so.purchase_order_drugs_id =:purchaseOrderDrugsId and so.purchase_status_id>0";
    @Query(value = SQL1,nativeQuery = true)
    PurchaseOrderDrugs getPurchaseOrderDrug(@Param("purchaseOrderDrugsId") Long purchaseOrderDrugsId);

    //Teslim Alma Tarafından Kullanılıyor
    String SQL2 = "select * from purchase_order_drugs pod where pod.customer_order_id=:customerOrderId and pod.drug_card_id=:drugCardId";
    @Query(value = SQL2,nativeQuery = true)
    List<PurchaseOrderDrugs> SearchInPurchaseOrdDrugs(@Param("drugCardId") Long drugCardId, @Param("customerOrderId") Long customerOrderId);

    String SQL3 = "select pod.* from purchase_order_drugs pod " +
            "join customer_order co ON co.customer_order_id = pod.customer_order_id " +
            "join customer_order_drugs cod on cod.customer_order_id = co.customer_order_id  and cod.drug_card_id = pod.drug_card_id " +
            "where cod.customer_order_drug_id =:customerOrderDrugId";
    @Query(value = SQL3,nativeQuery = true)
    Optional<PurchaseOrderDrugs> getOneForExporter(@Param("customerOrderDrugId") Long customerOrderDrugId);

    String SQL4 = "select pod.* from purchase_order_drugs pod  " +
            "join customer_order co on co.customer_order_id = pod.customer_order_id  " +
            "join customer_order_drugs cod on cod.customer_order_id = co.customer_order_id and cod.purchase_order_drugs_id = pod.purchase_order_drugs_id  " +
            "where pod.purchase_status_id = 5 and cod.customer_order_drug_id =:customerOrderDrugId ";
    @Query(value = SQL4,nativeQuery = true)
    Optional<PurchaseOrderDrugs> getPurchasewitStatus5(@Param("customerOrderDrugId") Long customerOrderDrugId);

    String SQL5 = "select pod.* from purchase_order_drugs pod  " +
            "join customer_order co on co.customer_order_id = pod.customer_order_id  " +
            "join customer_order_drugs cod on cod.customer_order_id = co.customer_order_id and cod.purchase_order_drugs_id = pod.purchase_order_drugs_id  " +
            "where cod.customer_order_drug_id =:customerOrderDrugId ";
    @Query(value = SQL5,nativeQuery = true)
    Optional<PurchaseOrderDrugs> getPurchaseForControllingStatus(@Param("customerOrderDrugId") Long customerOrderDrugId);
}
