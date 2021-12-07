package com.via.ecza.repo;

import com.via.ecza.dto.DepotGroupByExpDateDto;
import com.via.ecza.entity.Depot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepotRepository extends JpaRepository<Depot, Long> {
    Optional<Depot> findByDrugBarcode(String drugBarcode);
    Optional<Depot> findByItsNo(String itsNo);

    //Satın alma modulu //Stocktan sipariş için ilaç sayısı çekme
    String SQL1 = "select * from depot d where  d.drug_card_id=:drugCard and d.depot_status_id=10 "; //10stokta olma
    @Query(value = SQL1,nativeQuery = true)
    List<Depot> getStockByOrder( @Param("drugCard") Long drugCard);


    ///Satın alma modulu // Stocktaki ilaçları sipariş için
    String SQL2 = "select * from depot d  where d.drug_card_id =:drugCard and d.depot_status_id =10 order by expiration_date  ";
    @Query(value = SQL2,nativeQuery = true)
    List<Depot> setStockByOrder( @Param("drugCard") Long drugCard);

    //Satın alma modulu // Stocktaki ilaçları tarihe göre gruplama //Count u depot size la alabilirim gerekirse
    String SQL3 = "select d.drug_card_id  as drugCardId, d.expiration_date as expDate, cast( count(*) as varchar)  as mytext from depot d where d.depot_status_id=10 and d.drug_card_id=:drugCard  group by d.expiration_date, d.drug_card_id   ";
    @Query(value = SQL3,nativeQuery = true)
    List<Object> getDrugsByDate(@Param("drugCard") Long drugCard);

    //siparişe bağlı olan depo ve stok sayısı komple alınır imha olanlar dahil
    String SQL4 = "select count(*) from depot d where d.customer_supply_order_id=:customerSupplyOrderId";
    @Query(value = SQL4,nativeQuery = true)
    Integer countOfDrugsInDepot(@Param("customerSupplyOrderId") Long customerSupplyOrderId);

    String SQL5 = "select d2.*  from   depot d2 join customer_supply_order cso on cso.customer_supply_order_id = d2.customer_supply_order_id " +
            " where d2.depot_status_id in (1,2) and d2.customer_supply_order_id =:customerSupplyOrderId ";
    @Query(value = SQL5,nativeQuery = true)
    Page<Depot> packagingDrugsFromDepot(@Param("customerSupplyOrderId") Long customerSupplyOrderId, Pageable page);

    //Satın alma modulu // Stocktaki ilaçları sipariş için ayrılanları silme . burdada set edilebilinir aslında
    String SQL6 = "select * from depot d  where d.depot_status_id =4  and d.customer_supply_order_id=:customerSupplyOrder order by expiration_date  ";
    @Query(value = SQL6,nativeQuery = true)
    List<Depot> reverseOrderByStock( @Param("customerSupplyOrder") Long customerSupplyOrder);

    //sadece depodaki ilaçların sayısı 
    String SQL7 = "select count(*) from depot d where d.customer_supply_order_id=:customerSupplyOrderId and d.depot_status_id=1";
    @Query(value = SQL7,nativeQuery = true)
    Integer countOfDrugsOnlyDepot(@Param("customerSupplyOrderId") Long customerSupplyOrderId);

    //sadece stoktaki ilaçların sayısı depo dahil değil
    String SQL8 = "select count(*) from depot d where d.customer_supply_order_id=:customerSupplyOrderId and d.depot_status_id=10";
    @Query(value = SQL8,nativeQuery = true)
    Integer countOfDrugsOnlyStock(@Param("customerSupplyOrderId") Long customerSupplyOrderId);

//    String SQL9 = "select * from depot d where d.customer_order_id =:customerOrderId and d.its_no =:itsNo and d.lot_no =:lotNo and d.serial_number =:serialNumber and d.drug_barcode =:drugBarcode";
//    @Query(value = SQL9,nativeQuery = true)
//    List<Depot> findAll( @Param("customerOrderId") Long customerOrderId);

    String SQL9 = "select * from depot d where d.depot_status_id IN(1,10) and d.customer_order_id =:customerOrderId and d.lot_no =:lotNo and d.drug_barcode =:drugBarcode and d.serial_number =:serialNumber";
    @Query(value = SQL9,nativeQuery = true)
    Optional<Depot> findSingleDepotForPack(@Param("customerOrderId") Long customerOrderId,
                                           @Param("lotNo") String lotNo,
                                           @Param("drugBarcode") String drugBarcode,
                                           @Param("serialNumber") String serialNumber);


    //Satın alma modulu //İade ilaçlarını iptal etme
    String SQL10 = "select * from depot d where d.refund_offer_id=:refundOffer and d.depot_status_id=5 "; //15 iade siparişi olma 5 iade talebi statusu
    @Query(value = SQL10,nativeQuery = true)
    List<Depot> cancelRefundOffer(@Param("refundOffer") Long refundOffer);

    //Satın alma modulu //İade ilaçlarını kabul etme
    String SQL11 = "select * from depot d where d.refund_offer_id=:refundOffer and d.depot_status_id=5 "; //15 iade siparişi olma 5 iade talebi statusu
    @Query(value = SQL11,nativeQuery = true)
    List<Depot> acceptRefund(@Param("refundOffer") Long refundOffer);


    //Satın alma modulu // iade siparişi satın alma modulu tarafından iptali
    String SQL12 = "select * from depot d where d.refund_id=:refund and d.depot_status_id=15"; //15 iade siparişi olma 5 iade talebi statusu
    @Query(value = SQL12,nativeQuery = true)
    List<Depot> cancelRefundOrder(@Param("refund") Long refund);

    List<Depot> findBySmallBoxId(Long smallBoxId);

    String SQL13 = "select * from depot d join small_box sb on sb.small_box_id = d.small_box_id where d.depot_status_id = 2 and d.small_box_id =:smallBoxId ";
    @Query(value = SQL13,nativeQuery = true)
    List<Depot> getAllDrugWithSmallBoxIdAndDepotStatus(@Param("smallBoxId") Long smallBoxId);


    String SQL14 = "select * from depot d where  d.lot_no =:lotNo and d.drug_barcode =:drugBarcode and d.serial_number =:serialNumber";
    @Query(value = SQL14,nativeQuery = true)
    Optional<Depot> controlDepotForRemovingFromSmallBox(
            @Param("lotNo") String lotNo,
            @Param("drugBarcode") String drugBarcode,
            @Param("serialNumber") String serialNumber);

    String SQL18 = "select * from depot d where d.lot_no =:lotNo and d.drug_barcode =:drugBarcode and d.serial_number =:serialNumber";
    @Query(value = SQL18,nativeQuery = true)
    Optional<Depot> controlDepotForRemovingFromBox(
            @Param("lotNo") String lotNo,
            @Param("drugBarcode") String drugBarcode,
            @Param("serialNumber") String serialNumber);

    //siparişe bağlı olan depo ve stok sayısı komple alınır imha olanlar dahil
    String SQL15 = "select count(*) from depot d where d.customer_order_id=:customerOrderId and (d.depot_status_id=1 or d.depot_status_id=10)";
    @Query(value = SQL15,nativeQuery = true)
    Long countOfDrugsOnlyDepotAndStock(@Param("customerOrderId") Long customerOrderId);

    //sadece depodaki ilaçların sayısı customer order'a göre
    String SQL16 = "select count(*) from depot d where d.customer_order_id=:customerOrderId and d.depot_status_id=1";
    @Query(value = SQL16,nativeQuery = true)
    Long countOfDrugsOnlyDepotToCO(@Param("customerOrderId") Long customerOrderId);

    //sadece stoktaki ilaçların sayısı depo dahil değil customer order'a göre
    String SQL17 = "select count(*) from depot d where d.customer_order_id=:customerOrderId and d.depot_status_id=10";
    @Query(value = SQL17,nativeQuery = true)
    Long countOfDrugsOnlyStockToCO(@Param("customerOrderId") Long customerOrderId);


    String SQL19 = "select * from depot d where d.depot_status_id in (1,10) and d.customer_order_id =:customerOrderId and d.lot_no =:lotNo and d.drug_barcode =:drugBarcode and d.serial_number =:serialNumber";
    @Query(value = SQL19,nativeQuery = true)
    Optional<Depot> findSingleDepotForSmallBox(@Param("customerOrderId") Long customerOrderId,
                                           @Param("lotNo") String lotNo,
                                           @Param("drugBarcode") String drugBarcode,
                                           @Param("serialNumber") String serialNumber);

    String SQL20 = "select * from depot d where d.customer_order_id =:customerOrderId and d.lot_no =:lotNo and d.drug_barcode =:drugBarcode and d.serial_number =:serialNumber";
    @Query(value = SQL20,nativeQuery = true)
    Optional<Depot> controlForBox(@Param("customerOrderId") Long customerOrderId,
                                           @Param("lotNo") String lotNo,
                                           @Param("drugBarcode") String drugBarcode,
                                           @Param("serialNumber") String serialNumber);

    //sadece iadesi kesinleşen ilaçların sayısı refund order'a göre
    String SQL21 = "select count(*) from depot d where d.refund_id=:refundId and d.depot_status_id=15";
    @Query(value = SQL21,nativeQuery = true)
    Integer countOfDrugsOnlyDepotForRefund(@Param("refundId") Long refundId);


    String SQL22 = "select count(*) from depot d where d.drug_barcode=:drugBarcode and d.customer_supply_order_id=:customerSupplyOrderId and (d.depot_status_id=1)";
    @Query(value = SQL22,nativeQuery = true)
    Integer countOfDrugsDepotBarcodeAndCsoId( @Param("drugBarcode") String drugBarcode,
                                         @Param("customerSupplyOrderId") Long customerSupplyOrderId);

    String SQL23 = "select count(*) from depot d where d.drug_barcode=:drugBarcode and d.customer_supply_order_id=:customerSupplyOrderId and (d.depot_status_id=10)";
    @Query(value = SQL23,nativeQuery = true)
    Integer countOfDrugsStockBarcodeAndCsoId( @Param("drugBarcode") String drugBarcode,
                                         @Param("customerSupplyOrderId") Long customerSupplyOrderId);

    //sadece iadesi kesinleşen ilaçların listesi refund order'a göre
    String SQL24 = "select * from depot d where d.refund_id=:refundId and d.depot_status_id=15";
    @Query(value = SQL24,nativeQuery = true)
    List<Depot> DrugListOnlyDepotForRefund(@Param("refundId") Long refundId);

    //Cso'ya göre sadece depodaki ilaç listesi
    String SQL25 = "select * from depot d where d.customer_supply_order_id=:customerSupplyOrderId and d.depot_status_id=1 order by d.expiration_date ASC";
    @Query(value = SQL25,nativeQuery = true)
    List<Depot> getDrugListOnlyDepotToCso( @Param("customerSupplyOrderId") Long customerSupplyOrderId);

    //Sipariş Kaydırma İlaçları Stoğa Güncelleme
    String SQL26 = "UPDATE depot SET depot_status_id=10, status=0 WHERE customer_order_id=:customerOrderId and customer_supply_order_id=:customerSupplyOrderId and drug_card_id=:drugCardId and depot_status_id=1";
    @Modifying
    @Query(value = SQL26,nativeQuery = true)
    void updateDrugsFromDepotToStock( @Param("customerSupplyOrderId") Long customerSupplyOrderId,
                                         @Param("customerOrderId") Long customerOrderId,
                                         @Param("drugCardId") Long drugCardId);

    //Stoktan manuel tedarik
    String SQL27 = "select * from depot where customer_supply_order_id=:customerSupplyOrderId and drug_card_id=:drugCardId and depot_status_id=4";
    @Query(value = SQL27,nativeQuery = true)
    List<Depot> getReservedDrugListToCso( @Param("customerSupplyOrderId") Long customerSupplyOrderId,
                                      @Param("drugCardId") Long drugCardId);


    String SQL28 = "select * from depot where customer_supply_order_id=:customerSupplyOrderId and drug_card_id=:drugCardId and (depot_status_id=1 or depot_status_id=10) order by depot_id asc";
    @Query(value = SQL28,nativeQuery = true)
    List<Depot> getDrugListForExchange( @Param("customerSupplyOrderId") Long customerSupplyOrderId,
                                      @Param("drugCardId") Long drugCardId);

    String SQL29 = "select * from depot d where d.customer_order_id=:customerOrderId and d.customer_supply_order_id=:customerSupplyOrderId and d.drug_card_id=:drugCardId and (d.depot_status_id=1 or d.depot_status_id=10) order by d.depot_id asc";
    @Query(value = SQL29,nativeQuery = true)
    List<Depot> getDepotDrugListForChangeStatus(@Param("customerOrderId") Long customerOrderId,
                                                @Param("customerSupplyOrderId") Long customerSupplyOrderId,
                                                @Param("drugCardId") Long drugCardId);

    //Manuel İlaç Kutulama İçin
    String SQL30 = "select * from depot d2 join drug_card dc on dc.drug_card_id = d2.drug_card_id " +
            " join customer_order_drugs cod on cod.customer_order_id = d2.customer_order_id " +
            " where d2.box_id is null and dc.status = 2 and d2.customer_order_id=:customerOrderId and dc.drug_card_id=:drugCardId " +
            " and cod.customer_order_drug_id=:customerOrderDrugId order by d2.depot_id asc fetch first :quantity rows only ";
    @Query(value = SQL30,nativeQuery = true)
    List<Depot> findDrugsForManuelPackaging(@Param("customerOrderId") Long customerOrderId,
                                            @Param("customerOrderDrugId") Long customerOrderDrugId,
                                            @Param("quantity") Integer quantity,
                                            @Param("drugCardId") Long drugCardId);

    //Manuel İlaç Paketleme İçin
//    String SQL31 = "select * from depot d2 join drug_card dc on dc.drug_card_id = d2.drug_card_id " +
//            "where d2.small_box_id is null and d2.box_id is null and dc.status = 2 and dc.drug_card_id=:drugCardId " +
//            "order by d2.depot_id asc fetch first :quantity rows only ";
//    @Query(value = SQL31,nativeQuery = true)
//    List<Depot> findDrugsForManuelSmallBoxing(@Param("quantity") Integer quantity,
//                                            @Param("drugCardId") Long drugCardId);

    String SQL31 = "select * from depot d join drug_card dc on dc.drug_card_id = d.drug_card_id  " +
            "join customer_order_drugs cod  on cod.customer_order_id = d.customer_order_id  " +
            "where d.small_box_id is null and d.box_id is null and dc.status = 2 and dc.drug_card_id=:drugCardId  \n" +
            "and cod.customer_order_id =:customerOrderId and cod.customer_order_drug_id =:customerOrderDrugId  " +
            "order by d.depot_id asc fetch first :quantity rows only  ";
    @Query(value = SQL31,nativeQuery = true)
    List<Depot> findDrugsForManuelSmallBoxing(@Param("customerOrderId") Long customerOrderId,
                                              @Param("customerOrderDrugId") Long customerOrderDrugId,
                                              @Param("quantity") Integer quantity,
                                              @Param("drugCardId") Long drugCardId);

    String SQL32 = "select * from depot d join small_box sb on sb.small_box_id = d.small_box_id " +
            "where sb.small_box_no =:smallBoxNo ";
    @Query(value = SQL32,nativeQuery = true)
    List<Depot> drugsWithSmallBoxNo(@Param("smallBoxNo") String smallBoxNo);

    //İhracatta ilaçların kaçta kaçı depoya girdi göstermek için
    String SQL33 = "select count(d.*) from depot d " +
            "join customer_order co on co.customer_order_id = d.customer_order_id " +
            "join drug_card dc on dc.drug_card_id = d.drug_card_id " +
            "join customer_order_drugs cod on cod.drug_card_id = dc.drug_card_id " +
            "where d.customer_order_id =:customerOrderId and cod.customer_order_drug_id =:customerOrderDrugId";
    @Query(value = SQL33,nativeQuery = true)
    Integer countOfDrugs(@Param("customerOrderId") Long customerOrderId,
                                @Param("customerOrderDrugId") Long customerOrderDrugId);

    //Manuel İlaç Paketten Çıkartmak İçin
    String SQL34 = "select * from depot d join drug_card dc on dc.drug_card_id = d.drug_card_id " +
            "where d.small_box_id=:smallBoxId and dc.status = 2 " +
            "order by d.depot_id asc fetch first :quantity rows only ";
    @Query(value = SQL34,nativeQuery = true)
    List<Depot> findFakeDrugsBySmallBoxId(@Param("quantity") Integer quantity,
                                              @Param("smallBoxId") Long smallBoxId);



    String SQL35 = "select * from depot d where d.depot_status_id in (1,10) " +
            "and d.lot_no =:lotNo and d.drug_barcode =:drugBarcode and d.serial_number =:serialNumber";
    @Query(value = SQL35,nativeQuery = true)
    Optional<Depot> findSingleDepotForSmallBox(@Param("lotNo") String lotNo,
                                               @Param("drugBarcode") String drugBarcode,
                                               @Param("serialNumber") String serialNumber);

    //Manuel İlaç Kutudan Çıkartmak İçin
    String SQL36 = "select * from depot d join drug_card dc on dc.drug_card_id = d.drug_card_id " +
            "where d.box_id=:boxId and dc.status = 2 and d.customer_order_id=:customerOrderId " +
            "and d.drug_card_id=:drugCardId order by d.depot_id asc fetch first :quantity rows only ";
    @Query(value = SQL36,nativeQuery = true)
    List<Depot> findFakeDrugsByBoxId(@Param("quantity") Integer quantity,
                                     @Param("boxId") Long boxId,
                                     @Param("customerOrderId") Long customerOrderId,
                                     @Param("drugCardId") Long drugCardId);


    //stok ve depodaki ilaç sayısı teslim almada göstermek için
    String SQL37 = "select count(*) from depot d where d.customer_supply_order_id=:customerSupplyOrderId and d.customer_order_id=:customerOrderId and (d.depot_status_id=1 or d.depot_status_id=10)";
    @Query(value = SQL37,nativeQuery = true)
    Integer countOfDrugsStockAndDepotForOrderAcceptance(@Param("customerSupplyOrderId") Long customerSupplyOrderId, @Param("customerOrderId") Long customerOrderId);

    String SQL38 = "select count(d.small_box_id) as cnt from depot d  " +
            "left join customer_order_drugs  cod on cod.customer_order_id=d.customer_order_id  " +
            "where cod.customer_order_drug_id =:customerOrderDrugId and d.drug_card_id =:drugCardId and d.small_box_id is not null ";
    @Query(value = SQL38,nativeQuery = true)
    Long depotCountByCustomerOrderAndCustomerOrderDrugId(
            @Param("customerOrderDrugId") Long customerOrderDrugId,
            @Param("drugCardId") Long drugCardId);

    String SQL39 = "select * from depot d where d.customer_order_id !=:customerOrderId and d.lot_no =:lotNo and d.drug_barcode =:drugBarcode and d.serial_number =:serialNumber";
    @Query(value = SQL39,nativeQuery = true)
    Optional<Depot> controlForBoxAndCustomerOrder(@Param("customerOrderId") Long customerOrderId,
                                  @Param("lotNo") String lotNo,
                                  @Param("drugBarcode") String drugBarcode,
                                  @Param("serialNumber") String serialNumber);

    String SQL40 =  "select dc.drug_name, d.lot_no, count(dc.drug_name) as drug_count, min(d.expiration_date) from depot d " +
            "join box_drug_list bdl  on d.depot_id =bdl.depot_id " +
            "join drug_card dc on dc.drug_card_id = d.drug_card_id " +
            "where bdl.box_id =:boxId group by d.lot_no ,dc.drug_name";
    @Query(value = SQL40,nativeQuery = true)
    List<Object[]> getBoxDrugs(@Param("boxId") Long boxId);



    String SQL42 = "SELECT drug_card_id,drug_barcode, expiration_date, COUNT(depot_id) FROM depot where depot_status_id =1 GROUP BY expiration_date,drug_card_id,drug_barcode ORDER BY COUNT(depot_id) ASC";
    @Query(value = SQL42,nativeQuery = true)
    List<DepotGroupByExpDateDto> getDepotGroupByExpDate();

    String SQL43 = "select * from depot d where  " +
            "d.lot_no =:lotNo and d.drug_barcode =:drugBarcode and d.serial_number =:serialNumber " ;
    @Query(value = SQL43,nativeQuery = true)
    Optional<Depot> findSingleDepotForPack(@Param("lotNo") String lotNo,
                                           @Param("drugBarcode") String drugBarcode,
                                           @Param("serialNumber") String serialNumber);

    String SQL44 =  "select b.box_no, b.customer_box_no, dc.drug_name, d.lot_no, count(dc.drug_name) as drug_count, min(d.expiration_date), b.exact_box_weight from depot d " +
            "join box_drug_list bdl  on d.depot_id =bdl.depot_id " +
            "join drug_card dc on dc.drug_card_id = d.drug_card_id " +
            "join customer_order co on co.customer_order_id = d.customer_order_id " +
            "join box b on b.box_id = d.box_id " +
            "where b.status =1 and co.customer_order_id =:customerOrderId " +
            "group by b.box_no, b.customer_box_no, d.lot_no, dc.drug_name, b.exact_box_weight";
    @Query(value = SQL44,nativeQuery = true)
    List<Object[]> getLogisticBoxDrugList(@Param("customerOrderId") Long customerOrderId);

    String SQL45 = "select * from depot d " +
            "join box b on b.box_id = d.box_id " +
            "join customer_order co on co.customer_order_id = b.customer_order_id " +
            "where b.customer_order_id =:customerOrderId";
    @Query(value = SQL45,nativeQuery = true)
    List<Depot> findDrugsForOutFromDepot(@Param("customerOrderId") Long customerOrderId);

    String SQL46 = "select * from depot d where d.depot_status_id in (1,10) " +
            "and d.drug_barcode =:drugBarcode and d.serial_number =:serialNumber";
    @Query(value = SQL46,nativeQuery = true)
    Optional<Depot> findSingleDepotForSmallBoxToStockCounting(
            @Param("drugBarcode") String drugBarcode,
            @Param("serialNumber") String serialNumber);




    String SQL47 = "select * from depot d where d.refund_id=:refundId"; //15 iade siparişi olma 5 iade talebi statusu
    @Query(value = SQL47,nativeQuery = true)
    List<Depot> depotRefundDrugList(@Param("refundId") Long refundId);

    String SQL48 =  "select dc.drug_name, d.lot_no, count(dc.drug_name) as drug_count, min(d.expiration_date) from depot d " +
            "join box_drug_list bdl  on d.depot_id =bdl.depot_id " +
            "join drug_card dc on dc.drug_card_id = d.drug_card_id " +
            "where bdl.small_box_id =:smallBoxId group by d.lot_no ,dc.drug_name";
    @Query(value = SQL48,nativeQuery = true)
    List<Object[]> getSmallBoxDrugs(@Param("smallBoxId") Long smallBoxId);


}
