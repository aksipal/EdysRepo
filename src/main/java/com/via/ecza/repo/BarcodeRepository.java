package com.via.ecza.repo;

import com.via.ecza.entity.Barcode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BarcodeRepository extends JpaRepository<Barcode, Long> {
}
