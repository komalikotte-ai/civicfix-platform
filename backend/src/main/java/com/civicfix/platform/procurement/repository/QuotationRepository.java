package com.civicfix.platform.procurement.repository;

import com.civicfix.platform.procurement.entity.Quotation;
import com.civicfix.platform.procurement.enums.QuotationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, Long> {

    Optional<Quotation> findByQuotationNumber(String quotationNumber);

    boolean existsByQuotationNumber(String quotationNumber);

    List<Quotation> findByPurchaseRequestId(Long purchaseRequestId);

    List<Quotation> findBySupplierId(Long supplierId);

    @Query("SELECT q FROM Quotation q WHERE " +
            "(:supplierId IS NULL OR q.supplier.id = :supplierId) AND " +
            "(:departmentId IS NULL OR q.department.id = :departmentId) AND " +
            "(:purchaseRequestId IS NULL OR (q.purchaseRequest IS NOT NULL AND q.purchaseRequest.id = :purchaseRequestId)) AND " +
            "(:status IS NULL OR q.status = :status)")
    Page<Quotation> searchQuotations(
            @Param("supplierId") Long supplierId,
            @Param("departmentId") Long departmentId,
            @Param("purchaseRequestId") Long purchaseRequestId,
            @Param("status") QuotationStatus status,
            Pageable pageable
    );
}
