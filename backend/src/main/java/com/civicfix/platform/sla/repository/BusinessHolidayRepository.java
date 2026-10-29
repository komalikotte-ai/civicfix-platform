package com.civicfix.platform.sla.repository;

import com.civicfix.platform.sla.entity.BusinessHoliday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BusinessHolidayRepository extends JpaRepository<BusinessHoliday, Long> {

    List<BusinessHoliday> findByOrganizationIdAndActiveTrue(Long organizationId);

    List<BusinessHoliday> findByOrganizationIdAndDepartmentIdAndActiveTrue(Long organizationId, Long departmentId);

    List<BusinessHoliday> findByOrganizationIdAndDepartmentIsNullAndActiveTrue(Long organizationId);

    List<BusinessHoliday> findByHolidayDate(LocalDate holidayDate);
}
