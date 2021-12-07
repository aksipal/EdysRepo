package com.via.ecza.repo;

import com.via.ecza.entity.PurchaseOrderDrugs;
import com.via.ecza.entity.Supplier;
import com.via.ecza.entity.SupplierOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierOfferRepository extends JpaRepository<SupplierOffer, Long> {

    String SQL1 = "select * from supplier_offer so where so.purchase_order_drugs_id =:purchaseOrderDrugs and so.supplier_offer_status_id between 0 and 49";
    @Query(value = SQL1,nativeQuery = true)
    List<SupplierOffer> getByPurchaseOrderDrugs(@Param("purchaseOrderDrugs") Long purchaseOrderDrugs);

    List<SupplierOffer> findByPurchaseOrderDrugs(PurchaseOrderDrugs purchaseOrderDrugs);


    String SQL2 = "select * from supplier_offer so where so.supplier_id =:supplier and so.supplier_offer_status_id=10";
    @Query(value = SQL2,nativeQuery = true)
    List<SupplierOffer> getBySupplier(@Param("supplier") Long supplier);

    String SQL3 = "select sum(offered_totality) from supplier_offer so where so.purchase_order_drugs_id =:purchaseOrderDrugsId and drug_card_id=:drugCard and so.supplier_offer_status_id>9 and so.supplier_offer_status_id<21";
    @Query(value = SQL3,nativeQuery = true)
    Integer getSumOfOffers(@Param("purchaseOrderDrugsId") Long customerOrderDrugId,@Param("drugCard") Long drugCard);



}
