package com.via.ecza.repo;

import com.via.ecza.entity.PtsInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PtsInformationRepository extends JpaRepository<PtsInformation, Long> {

    List<PtsInformation> findByBoxBarcode(String boxBarcode);

    //koli barkoduna sahip olan eski ilaç var ise hepsi silinir
    String SQL1 = "delete from pts_information p where p.box_barcode = :box_barcode";
    @Modifying
    @Query(value = SQL1,nativeQuery = true)
    int deletePreviousBoxBarcode(@Param("box_barcode") String box_barcode);

    //koli barkoduna sahip olan eski ilaç var ise hepsinin statusu 1 yapılır
    String SQL2 = "UPDATE pts_information SET status=1 WHERE box_barcode=:boxBarcode and status=0";
    @Modifying
    @Query(value = SQL2,nativeQuery = true)
    void updatePreviousBoxBarcodeStatusTo1( @Param("boxBarcode") String boxBarcode);


    String SQL3 = "select * from pts_information p where p.box_barcode = :box_barcode and p.status=0";
    @Query(value = SQL3,nativeQuery = true)
    List<PtsInformation> findByBoxBarcode2(@Param("box_barcode") String box_barcode);

    String SQL4 = "select count(*) from pts_information p where p.box_barcode = :box_barcode and p.status=0";
    @Query(value = SQL4,nativeQuery = true)
    int qrCodeListFromBarcode(@Param("box_barcode") String box_barcode);

}
