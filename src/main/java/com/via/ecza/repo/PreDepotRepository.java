package com.via.ecza.repo;

import com.via.ecza.entity.CustomerSupplyOrder;
import com.via.ecza.entity.PreDepot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreDepotRepository extends JpaRepository<PreDepot, Long> {

    List<PreDepot> findByCustomerSupplyOrder(CustomerSupplyOrder customerSupplyOrder);

//    String SQL1 = "select * from pre_depot pd join customer_order co ON pd.customer_order_id = co.customer_order_id \n" +
//            "join customer_supply_order cso on pd.customer_supply_order_id = cso.customer_supply_order_id \n" +
//            "where co.customer_order_no = :customerOrderNo and cso.supply_order_no = :supplyOrderNo";
//    @Query(value = SQL1,nativeQuery = true)
//    List<PreDepot> getAllWithCustomerOrderNoAndSupplyOrderNo(@Param("customerOrderNo") String customerOrderNo, @Param("supplyOrderNo") String supplyOrderNo);

    String SQL1 = "select * from pre_depot pd join customer_supply_order cso on pd.customer_supply_order_id = cso.customer_supply_order_id " +
            "where cso.customer_supply_order_id = :customerSupplyOrderId and pd.pre_depot_status_id !=1 order by pd.drug_expiration_date desc";
    @Query(value = SQL1,nativeQuery = true)
    List<PreDepot> getAllWithCustomerSupplyOrderId(@Param("customerSupplyOrderId") Long customerSupplyOrderId);

    String SQL2 = "select * from pre_depot pd where pd.customer_order_id=1 and pd.pre_depot_status_id !=1 order by pd.drug_expiration_date desc";
    @Query(value = SQL2,nativeQuery = true)
    List<PreDepot> getAllWithCustomerOrderId();

    String SQL3 = "delete from pre_depot pd where pd.customer_supply_order_id = :customerSupplyOrderId and (pd.pre_depot_status_id =5 or pd.pre_depot_status_id =6)";
    @Modifying
    @Query(value = SQL3,nativeQuery = true)
    int deletePreDepotStatus5_6(@Param("customerSupplyOrderId") Long customerSupplyOrderId);

    String SQL4 = "delete from pre_depot pd where pd.customer_supply_order_id is null and pd.customer_order_id is null";
    @Modifying
    @Query(value = SQL4,nativeQuery = true)
    int deletePreDepotOrderIsNull();

    String SQL5 = "select * from pre_depot pd where pd.customer_supply_order_id is null and pd.customer_order_id is null";
    @Query(value = SQL5,nativeQuery = true)
    List<PreDepot> getAllPreDepotOrderIsNull();

}
