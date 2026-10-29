package com.civicfix.platform.sla.dto;

import com.civicfix.platform.sla.entity.BusinessCalendar;

import java.time.LocalTime;

public class BusinessCalendarDto {

    private Long id;
    private Long organizationId;
    private Long departmentId;
    private String departmentName;
    private String name;
    private String workingDays;
    private LocalTime startTime;
    private LocalTime endTime;
    private String timeZone;
    private boolean is24x7;

    public BusinessCalendarDto() {
    }

    public static BusinessCalendarDto fromEntity(BusinessCalendar entity) {
        if (entity == null) return null;
        BusinessCalendarDto dto = new BusinessCalendarDto();
        dto.setId(entity.getId());
        if (entity.getOrganization() != null) {
            dto.setOrganizationId(entity.getOrganization().getId());
        }
        if (entity.getDepartment() != null) {
            dto.setDepartmentId(entity.getDepartment().getId());
            dto.setDepartmentName(entity.getDepartment().getName());
        }
        dto.setName(entity.getName());
        dto.setWorkingDays(entity.getWorkingDays());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setTimeZone(entity.getTimeZone());
        dto.setIs24x7(entity.isIs24x7());
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(String workingDays) {
        this.workingDays = workingDays;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public boolean isIs24x7() {
        return is24x7;
    }

    public void setIs24x7(boolean is24x7) {
        this.is24x7 = is24x7;
    }
}
