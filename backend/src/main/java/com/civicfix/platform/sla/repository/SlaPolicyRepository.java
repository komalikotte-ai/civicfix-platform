package com.civicfix.platform.sla.repository;

import com.civicfix.platform.complaints.enums.ComplaintPriority;
import com.civicfix.platform.complaints.enums.ComplaintSeverity;
import com.civicfix.platform.sla.entity.SlaPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SlaPolicyRepository extends JpaRepository<SlaPolicy, Long> {

    Optional<SlaPolicy> findByOrganizationIdAndDepartmentIdAndPriorityAndActiveTrue(
            Long organizationId, Long departmentId, ComplaintPriority priority);

    Optional<SlaPolicy> findByOrganizationIdAndDepartmentIsNullAndPriorityAndActiveTrue(
            Long organizationId, ComplaintPriority priority);

    List<SlaPolicy> findByOrganizationIdAndActiveTrue(Long organizationId);

    List<SlaPolicy> findByDepartmentIdAndActiveTrue(Long departmentId);

    @Query("SELECT p FROM SlaPolicy p WHERE " +
           "p.organization.id = :orgId AND p.active = true AND " +
           "(:deptId IS NULL OR p.department.id = :deptId OR p.department IS NULL) AND " +
           "(:category IS NULL OR LOWER(p.category) = LOWER(:category) OR p.category IS NULL) AND " +
           "(:priority IS NULL OR p.priority = :priority) AND " +
           "(:severity IS NULL OR p.severity = :severity OR p.severity IS NULL) " +
           "ORDER BY " +
           "CASE WHEN p.department.id = :deptId THEN 1 ELSE 2 END, " +
           "CASE WHEN LOWER(p.category) = LOWER(:category) THEN 1 ELSE 2 END, " +
           "CASE WHEN p.severity = :severity THEN 1 ELSE 2 END")
    List<SlaPolicy> findMatchingPolicies(@Param("orgId") Long orgId,
                                         @Param("deptId") Long deptId,
                                         @Param("category") String category,
                                         @Param("priority") ComplaintPriority priority,
                                         @Param("severity") ComplaintSeverity severity);
}
