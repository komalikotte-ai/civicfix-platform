package com.civicfix.platform.workorders.repository;

import com.civicfix.platform.workorders.entity.WorkOrder;
import com.civicfix.platform.workorders.enums.WorkOrderPriority;
import com.civicfix.platform.workorders.enums.WorkOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    Optional<WorkOrder> findByWorkOrderNumber(String workOrderNumber);

    boolean existsByWorkOrderNumber(String workOrderNumber);

    List<WorkOrder> findByComplaintId(Long complaintId);

    Optional<WorkOrder> findFirstByComplaintIdAndStatusNot(Long complaintId, WorkOrderStatus status);

    boolean existsByComplaintIdAndStatusNot(Long complaintId, WorkOrderStatus status);

    Page<WorkOrder> findByDepartmentId(Long departmentId, Pageable pageable);

    Page<WorkOrder> findByAssignedWorkerId(Long workerId, Pageable pageable);

    Page<WorkOrder> findByAssignedSupervisorId(Long supervisorId, Pageable pageable);

    Page<WorkOrder> findByAssetId(Long assetId, Pageable pageable);
    List<WorkOrder> findByAssetId(Long assetId);

    @Query("SELECT w FROM WorkOrder w WHERE " +
            "(:workOrderNumber IS NULL OR LOWER(w.workOrderNumber) LIKE LOWER(CONCAT('%', :workOrderNumber, '%'))) AND " +
            "(:complaintId IS NULL OR (w.complaint IS NOT NULL AND w.complaint.id = :complaintId)) AND " +
            "(:complaintTrackingNumber IS NULL OR (w.complaint IS NOT NULL AND LOWER(w.complaint.trackingNumber) LIKE LOWER(CONCAT('%', :complaintTrackingNumber, '%')))) AND " +
            "(:departmentId IS NULL OR w.department.id = :departmentId) AND " +
            "(:wardId IS NULL OR (w.ward IS NOT NULL AND w.ward.id = :wardId)) AND " +
            "(:assignedWorkerId IS NULL OR (w.assignedWorker IS NOT NULL AND w.assignedWorker.id = :assignedWorkerId)) AND " +
            "(:assignedSupervisorId IS NULL OR (w.assignedSupervisor IS NOT NULL AND w.assignedSupervisor.id = :assignedSupervisorId)) AND " +
            "(:status IS NULL OR w.status = :status) AND " +
            "(:priority IS NULL OR w.priority = :priority) AND " +
            "(:startDate IS NULL OR w.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR w.createdAt <= :endDate) AND " +
            "(:keyword IS NULL OR LOWER(w.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(w.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(w.workOrderNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR (w.addressLine IS NOT NULL AND LOWER(w.addressLine) LIKE LOWER(CONCAT('%', :keyword, '%'))))")
    Page<WorkOrder> searchWorkOrders(
            @Param("workOrderNumber") String workOrderNumber,
            @Param("complaintId") Long complaintId,
            @Param("complaintTrackingNumber") String complaintTrackingNumber,
            @Param("departmentId") Long departmentId,
            @Param("wardId") Long wardId,
            @Param("assignedWorkerId") Long assignedWorkerId,
            @Param("assignedSupervisorId") Long assignedSupervisorId,
            @Param("status") WorkOrderStatus status,
            @Param("priority") WorkOrderPriority priority,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
