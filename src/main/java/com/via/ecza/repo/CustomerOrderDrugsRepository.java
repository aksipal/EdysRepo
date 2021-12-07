package com.via.ecza.repo;

import com.via.ecza.entity.CustomerOrder;
import com.via.ecza.entity.CustomerOrderDrugs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerOrderDrugsRepository extends JpaRepository<CustomerOrderDrugs, Long> {

    Optional<CustomerOrderDrugs> findByCustomerOrderDrugIdAndCustomerOrder(Long customerOrderId, CustomerOrder customerOrder);
    List<CustomerOrderDrugs> findByCustomerOrder( CustomerOrder customerOrder);

    Optional<CustomerOrderDrugs> findByPurchaseOrderDrugsId(Long purchaseOrderDrugsId);

    String SQL1 = "select * from customer_order_drugs cod join customer_order co on co.customer_order_id =cod.customer_order_id where  co.status =1 \n" +
            "and co.order_status_id = 4 " +
            "and cod.drug_card_id  = 3";
    @Query(value = SQL1,nativeQuery = true)
    List<CustomerOrderDrugs> getAllDrugsForManager(@Param("drugCardId") Long drugCardId);

    //Teslim Alma Tarafından Kullanılıyor
    String SQL2 = "select * from customer_order_drugs cod where cod.customer_order_id=:customerOrderId and cod.drug_card_id=:drugCardId";
    @Query(value = SQL2,nativeQuery = true)
    List<CustomerOrderDrugs> SearchInCustOrdDrugs(@Param("drugCardId") Long drugCardId,@Param("customerOrderId") Long customerOrderId);

    //Teslim Alma Tarafından Kullanılıyor
    //Yurtdışı Siparişte İstenen Genel Toplam İlaç Sayısı
    String SQL3 = "select sum(cod.total_quantity) from customer_order_drugs cod where cod.customer_order_id =:customerOrderId";
    @Query(value = SQL3,nativeQuery = true)
    Long sumOfTotalQuantityInCOD(@Param("customerOrderId") Long customerOrderId);

    String SQL4 = "select * from customer_order_drugs cod  " +
            "left join depot d on d.customer_order_id  = cod.customer_order_id  " +
            "and d.drug_card_id = cod.drug_card_id  " +
            "where d.depot_id =:depotId and d.customer_order_id =:customerOrderId" ;
    @Query(value = SQL4,nativeQuery = true)
    Optional<CustomerOrderDrugs> getOneForPackaging(@Param("customerOrderId") Long customerOrderId,
                                                    @Param("depotId") Long depotId);


//    String SQL4V2 = "select cod.* from customer_order_drugs cod join customer_order co on co.customer_order_id = cod.customer_order_id  " +
//            "join depot d on d.customer_order_id = co.customer_order_id and d.drug_card_id = cod.drug_card_id where d.depot_id =:depotId and cod.customer_order_drug_id =:customerOrderDrugId" ;
//    @Query(value = SQL4V2,nativeQuery = true)
//    Optional<CustomerOrderDrugs> getOneForPackaging(@Param("customerOrderDrugId") Long customerOrderDrugId, @Param("depotId") Long depotId);


    String SQL9 = "select cod.* from customer_order co " +
            "join customer_order_drugs cod  on cod.customer_order_id = co.customer_order_id " +
            "where co.order_status_id in (30,40,50) and co.status = 1 order by co.customer_order_id";
    @Query(value = SQL9,nativeQuery = true)
    Page<CustomerOrderDrugs> getDrugsForPackaging(Pageable page);

    String SQL10 = "select cod.* from customer_order co " +
            "join customer_order_drugs cod  on cod.customer_order_id = co.customer_order_id " +
            "where co.order_status_id in (30,40,50) and cod.drug_card_id =:drugCardId and co.status = 1 order by co.customer_order_id";
    @Query(value = SQL10,nativeQuery = true)
    Page<CustomerOrderDrugs> getDrugsForPackagingWithDrug(@Param("drugCardId") Long drugCardId, Pageable page);

    String SQL5= "select cod.* from customer_order_drugs cod " +
            "join customer_order co on co.customer_order_id = cod.customer_order_id " +
            "join customer c on co.customer_id = c.customer_id " +
            "where c.customer_id =:customerId and cod.customer_order_drug_id =:customerOrderDrugId " +
            "and co.customer_order_id =:customerOrderId" ;
    @Query(value = SQL5,nativeQuery = true)
    Optional<CustomerOrderDrugs> getSingleDrugForManager(@Param("customerId") Long customerId,@Param("customerOrderId") Long customerOrderId, @Param("customerOrderDrugId") Long customerOrderDrugId);

    String SQL6 = "select cod.* from customer_order_drugs cod " +
            "join customer_receipt cr on cr.customer_receipt_id = cod.customer_receipt_id " +
            //"join final_receipt fr on fr.final_receipt_id = cr.final_receipt_id " +
            "join invoice i on i.invoice_id = cr.invoice_id " +
            "where i.invoice_id =:invoiceId";
    @Query(value = SQL6,nativeQuery = true)
    List<CustomerOrderDrugs> getDrugsByInvoiceId(@Param("invoiceId") Long invoiceId);

    String SQL11 = "select * from customer_order_drugs cod " +
            "join campaign c on c.campaign_id = cod.campaign_id " +
            "where cod.purchase_order_drugs_id is null and cod.campaign_id =:campaignId" ;
    @Query(value = SQL11,nativeQuery = true)
    List<CustomerOrderDrugs> drugForDeleteWithCampaignNotPurchase( @Param("campaignId") Long campaignId);

    String SQL12 = "select * from customer_order_drugs cod " +
            "join campaign c on c.campaign_id = cod.campaign_id " +
            "where  cod.campaign_id =:campaignId" ;
    @Query(value = SQL12,nativeQuery = true)
    List<CustomerOrderDrugs> drugForDeleteWithCampaign( @Param("campaignId") Long campaignId);

    String SQL13 = "select * from customer_order_drugs cod " +
            "join customer_order co on co.customer_order_id = cod.customer_order_id " +
            "join customer c2 on c2.customer_id = co.customer_id " +
            "where cod.customer_order_id =:customerOrderId and c2.customer_id=:customerId " +
            "order by cod.is_deleted desc, cod.is_added_by_manager desc, cod.is_campaigned_drug desc, cod.created_date" ;
    @Query(value = SQL13,nativeQuery = true)
    List<CustomerOrderDrugs> getAllCustomerOrderDrugsForAdmin( @Param("customerOrderId") Long customerOrderId,
                                                               @Param("customerId") Long customerId);

    String SQL14 = "select * from customer_order_drugs cod " +
            "join customer_order co on co.customer_order_id = cod.customer_order_id " +
            "join customer c2 on c2.customer_id = co.customer_id " +
            "where cod.customer_order_id =:customerOrderId " +
            "and c2.customer_id=:customerId and co.user_id =:userId " +
            "order by cod.is_deleted desc, cod.is_added_by_manager desc, cod.is_campaigned_drug desc, cod.created_date" ;
    @Query(value = SQL14,nativeQuery = true)
    List<CustomerOrderDrugs> getAllCustomerOrderDrugsForExporter( @Param("customerOrderId") Long customerOrderId,
                                                                  @Param("customerId") Long customerId,
                                                                  @Param("userId") Long userId);

    String SQL15 = "select * from customer_order_drugs cod " +
            "join customer_order co on co.customer_order_id = cod.customer_order_id " +
            "join campaign c2 on c2.campaign_id = cod.campaign_id " +
            "where co.order_status_id < 15 and c2.campaign_id =:campaignId" ;
    @Query(value = SQL15,nativeQuery = true)
    List<CustomerOrderDrugs> findCustomerOrderDrugsWithCampaign(@Param("campaignId") Long campaignId);




    String SQL16 = " select cod.* from customer_order_drugs cod  " +
            "left join depot d on d.customer_order_id = cod.customer_order_id   " +
            "and cod.customer_order_id=d.customer_order_id and d.drug_card_id = cod.drug_card_id   " +
            "where d.depot_id =:depotId and d.customer_order_id=:customerOrderId  " +
            "and cod.customer_order_drug_id =:customerOrderDrugId " ;
    @Query(value = SQL16,nativeQuery = true)
    Optional<CustomerOrderDrugs> getOneForPackaging( @Param("customerOrderId") Long customerOrderId,
                                                     @Param("customerOrderDrugId") Long customerOrderDrugId,
                                                     @Param("depotId") Long depotId);

    String SQL17 = "select cod.* from customer_order_drugs cod where cod.customer_order_id =:receipt_id ";
    @Query(value = SQL17,nativeQuery = true)
    List<CustomerOrderDrugs> getByCustomerReceiptId(@Param("receipt_id") Long customerOrderId);
}
