package com.civicfix.platform.sla.dto;

import com.civicfix.platform.sla.entity.BusinessHoliday;

import java.time.LocalDate;

public class BusinessHolidayDto {

    private Long id;
    private Long organizationId;
    private Long departmentId;
    private String departmentName;
    private LocalDate holidayDate;
    private String name;
    private boolean recurring;

    public BusinessHolidayDto() {
    }

    public static BusinessHolidayDto fromEntity(BusinessHoliday entity) {
        if (entity == null) return null;
        BusinessHolidayDto dto = new BusinessHolidayDto();
        dto.setId(entity.getId());
        if (entity.getOrganization() != null) {
            dto.setOrganizationId(entity.getOrganization().getId());
        }
        if (entity.getDepartment() != null) {
            dto.setDepartmentId(entity.getDepartment().getId());
            dto.setDepartmentName(entity.getDepartment().getName());
        }
        dto.setHolidayDate(entity.getHolidayDate());
        dto.setName(entity.getName());
        dto.setRecurring(entity.isRecurring());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public LocalDate getHolidayDate() {
        return holidayDate;
    }

    public void setHolidayDate(LocalDate holidayDate) {
        this.holidayDate = holidayDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRecurring() {
        return recurring;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }
}
