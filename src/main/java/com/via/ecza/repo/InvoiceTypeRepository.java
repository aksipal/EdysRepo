package com.via.ecza.repo;

import com.via.ecza.entity.InvoiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceTypeRepository extends JpaRepository<InvoiceType, Long> {
}
