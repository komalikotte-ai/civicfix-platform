package com.civicfix.platform.inventory.repository;

import com.civicfix.platform.inventory.entity.PurchaseRequest;
import com.civicfix.platform.inventory.enums.PurchasePriority;
import com.civicfix.platform.inventory.enums.PurchaseRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, Long> {

    Optional<PurchaseRequest> findByRequestCode(String requestCode);

    @Query("SELECT pr FROM PurchaseRequest pr WHERE " +
            "(:departmentId IS NULL OR pr.department.id = :departmentId) AND " +
            "(:organizationId IS NULL OR pr.organization.id = :organizationId) AND " +
            "(:status IS NULL OR pr.status = :status) AND " +
            "(:priority IS NULL OR pr.priority = :priority) AND " +
            "(:workOrderId IS NULL OR (pr.workOrder IS NOT NULL AND pr.workOrder.id = :workOrderId)) AND " +
            "(:startDate IS NULL OR pr.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR pr.createdAt <= :endDate)")
    Page<PurchaseRequest> searchRequests(
            @Param("departmentId") Long departmentId,
            @Param("organizationId") Long organizationId,
            @Param("status") PurchaseRequestStatus status,
            @Param("priority") PurchasePriority priority,
            @Param("workOrderId") Long workOrderId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );
}
