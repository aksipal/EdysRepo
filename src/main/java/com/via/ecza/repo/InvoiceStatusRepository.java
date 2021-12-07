package com.via.ecza.repo;

import com.via.ecza.entity.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceStatusRepository extends JpaRepository<InvoiceStatus, Long> {
}
