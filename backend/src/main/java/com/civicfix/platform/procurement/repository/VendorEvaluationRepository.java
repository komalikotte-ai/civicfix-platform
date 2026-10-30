package com.civicfix.platform.procurement.repository;

import com.civicfix.platform.procurement.entity.VendorEvaluation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorEvaluationRepository extends JpaRepository<VendorEvaluation, Long> {

    List<VendorEvaluation> findBySupplierIdOrderByEvaluatedAtDesc(Long supplierId);

    Page<VendorEvaluation> findBySupplierId(Long supplierId, Pageable pageable);
}
