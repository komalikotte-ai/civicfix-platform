package com.civicfix.platform.complaints.repository;

import com.civicfix.platform.complaints.entity.Complaint;
import com.civicfix.platform.complaints.enums.ComplaintPriority;
import com.civicfix.platform.complaints.enums.ComplaintSeverity;
import com.civicfix.platform.complaints.enums.ComplaintStatus;
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
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    Optional<Complaint> findByTrackingNumber(String trackingNumber);

    Page<Complaint> findByOrganizationId(Long organizationId, Pageable pageable);

    Page<Complaint> findByCitizenId(Long citizenId, Pageable pageable);

    Page<Complaint> findByDepartmentId(Long departmentId, Pageable pageable);

    Page<Complaint> findByWardId(Long wardId, Pageable pageable);

    Page<Complaint> findByStatus(ComplaintStatus status, Pageable pageable);

    Page<Complaint> findByAssignedOfficerId(Long officerId, Pageable pageable);

    Page<Complaint> findByAssignedEmployeeId(Long employeeId, Pageable pageable);

    List<Complaint> findByDuplicateGroupId(String duplicateGroupId);

    List<Complaint> findByDuplicateOfId(Long duplicateOfId);

    List<Complaint> findByStatusIn(List<ComplaintStatus> statuses);

    List<Complaint> findByStatusNotIn(List<ComplaintStatus> statuses);

    Page<Complaint> findByAssetId(Long assetId, Pageable pageable);
    List<Complaint> findByAssetId(Long assetId);

    @Query("SELECT c FROM Complaint c WHERE " +
           "(:citizenId IS NULL OR c.citizen.id = :citizenId) AND " +
           "(:departmentId IS NULL OR c.department.id = :departmentId) AND " +
           "(:wardId IS NULL OR c.ward.id = :wardId) AND " +
           "(:assignedEmployeeId IS NULL OR c.assignedEmployee.id = :assignedEmployeeId) AND " +
           "(:category IS NULL OR LOWER(c.category) = LOWER(:category)) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:priority IS NULL OR c.priority = :priority) AND " +
           "(:severity IS NULL OR c.severity = :severity) AND " +
           "(:startDate IS NULL OR c.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR c.createdAt <= :endDate) AND " +
           "(:keyword IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.trackingNumber) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Complaint> searchComplaints(@Param("citizenId") Long citizenId,
                                     @Param("departmentId") Long departmentId,
                                     @Param("wardId") Long wardId,
                                     @Param("assignedEmployeeId") Long assignedEmployeeId,
                                     @Param("category") String category,
                                     @Param("status") ComplaintStatus status,
                                     @Param("priority") ComplaintPriority priority,
                                     @Param("severity") ComplaintSeverity severity,
                                     @Param("startDate") Instant startDate,
                                     @Param("endDate") Instant endDate,
                                     @Param("keyword") String keyword,
                                     Pageable pageable);
}
