package com.via.ecza.repo;

import com.via.ecza.entity.DepotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DepotStatusRepository extends JpaRepository<DepotStatus, Long> {

    String SQL1 = "select * from depot_status ds where ds.depot_status_id!=10 and ds.depot_status_id!=20 order by ds.status_name";
    @Query(value = SQL1,nativeQuery = true)
    List<DepotStatus> getDepotStatus();

}
