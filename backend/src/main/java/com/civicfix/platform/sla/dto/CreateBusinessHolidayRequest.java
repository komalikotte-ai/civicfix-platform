package com.civicfix.platform.sla.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class CreateBusinessHolidayRequest {

    @NotNull(message = "Organization ID is required")
    private Long organizationId;

    private Long departmentId;

    @NotNull(message = "Holiday date is required")
    private LocalDate holidayDate;

    @NotBlank(message = "Holiday name is required")
    private String name;

    private boolean recurring = false;

    public CreateBusinessHolidayRequest() {
    }

    public CreateBusinessHolidayRequest(Long organizationId, Long departmentId, LocalDate holidayDate, String name, boolean recurring) {
        this.organizationId = organizationId;
        this.departmentId = departmentId;
        this.holidayDate = holidayDate;
        this.name = name;
        this.recurring = recurring;
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
