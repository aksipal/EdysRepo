package com.via.ecza.repo;

import com.via.ecza.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {


    String SQL1 = " select c.* from campaign c where c.drug_card_id =:drugCardId  and  :cdate between c.campaign_start_date and c.campaign_end_date ";
    @Query(value = SQL1,nativeQuery = true)
    Campaign controlDate(@Param("drugCardId") Long drugCardId, @Param("cdate") Date cdate);


    String SQL2 = " select c.* from campaign c where c.drug_card_id =:drugCardId  and  " +
            " c.campaign_start_date <= :date1 and c.campaign_end_date > :date2 ";
    @Query(value = SQL2,nativeQuery = true)
    Campaign controlDate(@Param("drugCardId") Long drugCardId, @Param("date1") Date date1, @Param("date2") Date date2);
}
