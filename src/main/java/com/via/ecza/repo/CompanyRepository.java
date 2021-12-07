package com.via.ecza.repo;

import com.via.ecza.entity.Company;
import com.via.ecza.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface CompanyRepository extends JpaRepository<Company, Long> {

    List<Company> findByUser(User user);
}
