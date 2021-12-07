package com.via.ecza.repo;

import com.via.ecza.entity.QrCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QrCodeRepository extends JpaRepository <QrCode, Long> {
//Teslim Alma Kısmında Kullanmak İçin
    String SQL1 = "select * from qr_code where status=0 and camera_type=:cameraType";
    @Query(value = SQL1,nativeQuery = true)
    List<QrCode> findAllQrCodeForOrderAcceptance(int cameraType);

    List<QrCode> findByStatus(int status);
    List<QrCode> findByCameraType(int cameraType);
    List<QrCode> findByCameraTypeAndStatus(int cameraType, int status);

//    void deleteByCameraType(int cameraType);

    String SQL2 = "delete from qr_code where camera_type =:cameraType";
    @Modifying
    @Query(value = SQL2,nativeQuery = true)
    void deleteByCameraType(int cameraType);

    void deleteByStatus(int status);

}
