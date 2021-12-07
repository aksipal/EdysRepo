package com.via.ecza.repo;

import com.via.ecza.entity.VersionInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VersionInformationRepository extends JpaRepository <VersionInformation, Long> {
    String SQL1 = "select * from version_information where version_number =:versionNumber order by created_at asc";
    @Query(value = SQL1,nativeQuery = true)
    List<VersionInformation> getVersionInformationList(@Param("versionNumber") String versionNumber);

    String SQL2 = "select * from version_information order by version_information_id desc limit 1";
    @Query(value = SQL2,nativeQuery = true)
    Optional<VersionInformation> findForLastRecord();
}
