package com.civicfix.platform.sla.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public class CreateBusinessCalendarRequest {

    @NotNull(message = "Organization ID is required")
    private Long organizationId;

    private Long departmentId;

    @NotBlank(message = "Calendar name is required")
    private String name;

    @NotBlank(message = "Working days are required (comma separated)")
    private String workingDays = "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY";

    @NotNull(message = "Start time is required")
    private LocalTime startTime = LocalTime.of(9, 0);

    @NotNull(message = "End time is required")
    private LocalTime endTime = LocalTime.of(18, 0);

    private String timeZone = "Asia/Kolkata";

    private boolean is24x7 = false;

    public CreateBusinessCalendarRequest() {
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

    public boolean is24x7() {
        return is24x7;
    }

    public boolean isIs24x7() {
        return is24x7;
    }

    public void setIs24x7(boolean is24x7) {
        this.is24x7 = is24x7;
    }
}
