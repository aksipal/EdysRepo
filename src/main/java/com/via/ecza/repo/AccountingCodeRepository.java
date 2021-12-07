package com.via.ecza.repo;

import com.via.ecza.entity.AccountingCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountingCodeRepository extends JpaRepository<AccountingCode, Long> {

    String SQL1 = "select count(*) from accounting_code where name=:name and category_id=:categoryId";
    @Query(value = SQL1,nativeQuery = true)
    int findByNameAndCategory(@Param("name") String name,
                                           @Param("categoryId") Long categoryId);

    String SQL2 = "select count(*) from accounting_code where name=:name and category_id is null";
    @Query(value = SQL2, nativeQuery = true)
    int findByNameAndNullCategory(@Param("name") String name);

}
