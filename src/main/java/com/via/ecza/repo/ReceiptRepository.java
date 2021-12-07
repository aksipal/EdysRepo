package com.via.ecza.repo;

import com.via.ecza.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

    //List<Receipt> findByFinalReceipt(FinalReceipt finalReceipt);

    String SQL1 = "select * from receipt r where r.receipt_status_id =10";
    @Query(value = SQL1,nativeQuery = true)
    List<Receipt> getAllReceiptForAdmin();

    String SQL2 = "select * from receipt r where r.receipt_status_id =20";
    @Query(value = SQL2,nativeQuery = true)
    List<Receipt> getAllReceiptForManager();

    //hangi şirket üzerinde işlem yapılıyorsa ona uygun olan fişler listelenecek
    String SQL3 = "select r.* from receipt r inner join customer_supply_order cso on cso.receipt_id =r.receipt_id where r.receipt_status_id =30 and r.supplier_id =:supplierId and cso.other_company_id=:otherCompanyId group by r.receipt_id ";
    @Query(value = SQL3,nativeQuery = true)
    List<Receipt> getAllReceiptForAccounting(@Param("supplierId") Long supplierId, @Param("otherCompanyId") Long otherCompanyId);


}
