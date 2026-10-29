package com.civicfix.platform.sla.dto;

import com.civicfix.platform.complaints.enums.ComplaintPriority;
import com.civicfix.platform.complaints.enums.ComplaintSeverity;
import com.civicfix.platform.sla.entity.SlaPolicy;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class SlaPolicyDto {

    private Long id;

    @NotNull(message = "Organization ID is required")
    private Long organizationId;

    private Long departmentId;
    private String departmentName;

    private String category;

    @NotNull(message = "Complaint priority is required")
    private ComplaintPriority priority = ComplaintPriority.MEDIUM;

    private ComplaintSeverity severity;

    private int responseTimeHours = 2;

    @Min(value = 1, message = "Resolution time must be at least 1 hour")
    private int resolutionTimeHours = 48;

    @Min(value = 1, message = "Escalation time must be at least 1 hour")
    private int escalationTimeHours = 12;

    private int warningThresholdPercent = 75;

    private boolean businessHoursOnly = true;

    private Long calendarId;
    private String calendarName;

    private boolean active;

    public SlaPolicyDto() {
    }

    public static SlaPolicyDto fromEntity(SlaPolicy entity) {
        if (entity == null) return null;
        SlaPolicyDto dto = new SlaPolicyDto();
        dto.setId(entity.getId());
        if (entity.getOrganization() != null) {
            dto.setOrganizationId(entity.getOrganization().getId());
        }
        if (entity.getDepartment() != null) {
            dto.setDepartmentId(entity.getDepartment().getId());
            dto.setDepartmentName(entity.getDepartment().getName());
        }
        dto.setCategory(entity.getCategory());
        dto.setPriority(entity.getPriority());
        dto.setSeverity(entity.getSeverity());
        dto.setResponseTimeHours(entity.getResponseTimeHours());
        dto.setResolutionTimeHours(entity.getResolutionTimeHours());
        dto.setEscalationTimeHours(entity.getEscalationTimeHours());
        dto.setWarningThresholdPercent(entity.getWarningThresholdPercent());
        dto.setBusinessHoursOnly(entity.isBusinessHoursOnly());
        if (entity.getCalendar() != null) {
            dto.setCalendarId(entity.getCalendar().getId());
            dto.setCalendarName(entity.getCalendar().getName());
        }
        dto.setActive(entity.isActive());
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public ComplaintPriority getPriority() {
        return priority;
    }

    public void setPriority(ComplaintPriority priority) {
        this.priority = priority;
    }

    public ComplaintSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(ComplaintSeverity severity) {
        this.severity = severity;
    }

    public int getResponseTimeHours() {
        return responseTimeHours;
    }

    public void setResponseTimeHours(int responseTimeHours) {
        this.responseTimeHours = responseTimeHours;
    }

    public int getResolutionTimeHours() {
        return resolutionTimeHours;
    }

    public void setResolutionTimeHours(int resolutionTimeHours) {
        this.resolutionTimeHours = resolutionTimeHours;
    }

    public int getEscalationTimeHours() {
        return escalationTimeHours;
    }

    public void setEscalationTimeHours(int escalationTimeHours) {
        this.escalationTimeHours = escalationTimeHours;
    }

    public int getWarningThresholdPercent() {
        return warningThresholdPercent;
    }

    public void setWarningThresholdPercent(int warningThresholdPercent) {
        this.warningThresholdPercent = warningThresholdPercent;
    }

    public boolean isBusinessHoursOnly() {
        return businessHoursOnly;
    }

    public void setBusinessHoursOnly(boolean businessHoursOnly) {
        this.businessHoursOnly = businessHoursOnly;
    }

    public Long getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(Long calendarId) {
        this.calendarId = calendarId;
    }

    public String getCalendarName() {
        return calendarName;
    }

    public void setCalendarName(String calendarName) {
        this.calendarName = calendarName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
