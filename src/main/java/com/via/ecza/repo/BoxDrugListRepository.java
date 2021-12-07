package com.via.ecza.repo;

import com.via.ecza.dto.BoxDrugCheckListDto;
import com.via.ecza.entity.BoxDrugList;
import com.via.ecza.entity.Depot;
import com.via.ecza.entity.SmallBox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoxDrugListRepository extends JpaRepository<BoxDrugList, Long>  {

    List<BoxDrugList> findBySmallBox(SmallBox smallBox);

    Optional<BoxDrugList> findByDepot(Depot depot);

    String SQL1 = "select * from box_drug_list  where depot_id =:depotId";
    @Query(value = SQL1,nativeQuery = true)
    Optional<BoxDrugList> controlSingleDepot(@Param("depotId") Long depotId);
}
