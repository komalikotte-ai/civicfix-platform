package com.civicfix.platform.sla.repository;

import com.civicfix.platform.sla.entity.BusinessCalendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessCalendarRepository extends JpaRepository<BusinessCalendar, Long> {

    Optional<BusinessCalendar> findByOrganizationIdAndDepartmentIdAndActiveTrue(Long organizationId, Long departmentId);

    Optional<BusinessCalendar> findByOrganizationIdAndDepartmentIsNullAndActiveTrue(Long organizationId);

    Optional<BusinessCalendar> findByDepartmentIdAndActiveTrue(Long departmentId);

    List<BusinessCalendar> findByOrganizationIdAndActiveTrue(Long organizationId);
}
