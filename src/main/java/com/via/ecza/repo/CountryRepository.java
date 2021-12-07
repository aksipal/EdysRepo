package com.via.ecza.repo;

import com.via.ecza.entity.Country;
import com.via.ecza.entity.DrugCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, Long> {

	Country findByName(String name);
	List<Country>  findByNameContainingIgnoreCase(String sourceCountryForCalculation);

}
