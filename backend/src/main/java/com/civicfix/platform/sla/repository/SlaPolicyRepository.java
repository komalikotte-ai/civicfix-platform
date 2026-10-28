package com.civicfix.platform.sla.repository;

import com.civicfix.platform.complaints.enums.ComplaintPriority;
import com.civicfix.platform.sla.entity.SlaPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
