package com.via.ecza.repo;

import com.via.ecza.entity.DiscountSetting;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface DiscountSettingRepository extends CrudRepository<DiscountSetting, Long> {

    String SQL1 = "select * from discount_setting order by discount_setting_id desc limit 1";
    @Query(value = SQL1,nativeQuery = true)
    Optional<DiscountSetting> getLastDiscountSetting();
}
