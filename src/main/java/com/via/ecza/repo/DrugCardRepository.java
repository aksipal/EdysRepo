package com.via.ecza.repo;

import com.via.ecza.entity.DrugCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DrugCardRepository extends JpaRepository <DrugCard, Long> {

    Optional<DrugCard> findByDrugCode(Long drugCode);
    Optional<DrugCard> findByDrugName(String drugName);
    Optional<DrugCard> findByDrugCardId(Long drugCardId);


    String SQL1 = "select * from drug_card dc " +
            "join campaign c on c.drug_card_id = dc.drug_card_id " +
            "where c.campaign_id =:campaignId";
    @Query(value = SQL1,nativeQuery = true)
    Optional<DrugCard> getByCampaignId(@Param("campaignId") Long campaignId);

//    String SQL2 = "UPDATE drug_card SET is_active=false WHERE drug_name=:drugName and to_date(:nowDate,'dd.MM.yyyy') > validity_date";
//    @Modifying
//    @Query(value = SQL2,nativeQuery = true)
//    void updateIsActive(@Param("drugName") String drugName, @Param("nowDate")String nowDate);

    String SQL3 = "select * from drug_card dc where dc.drug_name=:drugName and dc.isActive=true";
    @Query(value = SQL3,nativeQuery = true)
    List<DrugCard> searchActiveDrugCardByDrugName(@Param("drugName") String drugName);

    //tüm ilaçların is_active alanı true olacak şekilde güncelleniyor
    String SQL4 = "UPDATE drug_card SET is_active=true";
    @Modifying
    @Query(value = SQL4,nativeQuery = true)
    void updateDrugsofIsActivetoTrue();

    //gelen barkodun is_active alanı false olacak şekilde güncelleniyor
    String SQL5 = "UPDATE drug_card SET is_active=false where drug_code=:drugCode";
    @Modifying
    @Query(value = SQL5,nativeQuery = true)
    void updateIsActivetoFalse(@Param("drugCode") Long drugCode);

}
