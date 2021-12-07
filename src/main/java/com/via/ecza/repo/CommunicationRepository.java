package com.via.ecza.repo;

import com.via.ecza.entity.Communication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommunicationRepository extends JpaRepository<Communication, Long> {

    Optional<Communication> findByCameraType(int cameraType);


    String SQL1 = "select * from communication c where camera_type=:cameraType " ;
    @Query(value = SQL1,nativeQuery = true)
    Optional<Communication> getSingleCommunication(@Param("cameraType")  int cameraType);

    String SQL2 = "delete from communication where camera_type =:cameraType";
    @Modifying
    @Query(value = SQL2,nativeQuery = true)
    void deleteByCameraType(int cameraType);
}
