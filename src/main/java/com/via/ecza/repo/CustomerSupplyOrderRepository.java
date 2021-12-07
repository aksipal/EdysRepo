package com.via.ecza.repo;


import com.via.ecza.entity.CustomerSupplyOrder;
import com.via.ecza.entity.PurchaseOrderDrugs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerSupplyOrderRepository extends JpaRepository<CustomerSupplyOrder,Long> {

    Optional<CustomerSupplyOrder> findBySupplyOrderNo(String supplyOrderNo);

    String SQL1 = "select * from customer_supply_order cso where cso.purchase_order_drugs_id =:purchaseOrderDrugs and cso.customer_supply_status_id>9 and cso.customer_supply_status_id<49";
    @Query(value = SQL1,nativeQuery = true)
    List<CustomerSupplyOrder> findByPurchaseOrderDrugsId(@Param("purchaseOrderDrugs") Long customerOrderDrugId);

    Optional<CustomerSupplyOrder> findByPurchaseOrderDrugs(PurchaseOrderDrugs purchaseOrderDrugs);


    String SQL2 = "select * from customer_supply_order cso where cso.supplier_id=:supplier and cso.customer_supply_status_id=10 ";
    @Query(value = SQL2,nativeQuery = true)
    List<CustomerSupplyOrder> getOrdersBySupplier(@Param("supplier") Long supplier);

    String SQL3 = "select cso.* from customer_supply_order cso " +
            "join purchase_order_drugs pod on pod.purchase_order_drugs_id = cso.purchase_order_drugs_id " +
            "join customer_order co on co.customer_order_id = pod.customer_order_id " +
            "join customer_order_drugs cod on co.customer_order_id = cod.customer_order_id and pod.drug_card_id = cod.drug_card_id " +
            "where co.order_status_id in(30,40,50,60) and co.customer_order_id =:customerOrderId";
    @Query(value = SQL3,nativeQuery = true)
    Page<CustomerSupplyOrder> getAllOrdersForPackaging( @Param("customerOrderId") Long customerOrderId, Pageable page);

    //Teslim Alma Tarafından Kullanılıyor
    String SQL4 = "select * from customer_supply_order cso where cso.drug_card_id=:drugCardId and cso.purchase_order_drugs_id=:purchaseOrderDrugId and cso.supplier_id=:supplierId";
    @Query(value = SQL4,nativeQuery = true)
    List<CustomerSupplyOrder> SearchInCustSuppOrder(@Param("drugCardId") Long drugCardId, @Param("purchaseOrderDrugId") Long purchaseOrderDrugId, @Param("supplierId") Long supplierId);

    String SQL5 = "select cso.* from customer_supply_order cso where cso.receipt_id=:receiptId ";
    @Query(value = SQL5,nativeQuery = true)
    List<CustomerSupplyOrder> getByReceiptId(@Param("receiptId") Long receiptId);

    String SQL6 = "select cso.* from customer_supply_order cso where cso.customer_supply_order_id=:customerSupplyOrderId ";
    @Query(value = SQL6,nativeQuery = true)
    Optional<CustomerSupplyOrder> getByCustomerSupplyOrderId(@Param("customerSupplyOrderId") Long customerSupplyOrderId);

    String SQL7 = "select cso.* from customer_supply_order cso " +
            "where cso.receipt_id is null and cso.supplier_id=:supplierId " +
            "and customer_supply_status_id = 50 and other_company_id=:otherCompanyId";
    @Query(value = SQL7,nativeQuery = true)
    List<CustomerSupplyOrder> getByOrder(@Param("supplierId") Long supplierId,@Param("otherCompanyId") Long otherCompanyId);

    String SQL8 = "select * from customer_supply_order cso " +
            "join depot d on d.customer_supply_order_id = cso.customer_supply_order_id " +
            "where d.depot_id =:depotId";
    @Query(value = SQL8,nativeQuery = true)
    Optional<CustomerSupplyOrder> findSingleCustomerSupplyOrderForSmallBoxStockCounting(@Param("depotId") Long depotId);


}