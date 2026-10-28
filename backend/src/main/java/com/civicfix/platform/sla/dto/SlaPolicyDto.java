package com.civicfix.platform.sla.dto;

import com.civicfix.platform.complaints.enums.ComplaintPriority;
import com.civicfix.platform.sla.entity.SlaPolicy;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class SlaPolicyDto {

    private Long id;

    @NotNull(message = "Organization ID is required")
    private Long organizationId;

    private Long departmentId;

    @NotNull(message = "Complaint priority is required")
    private ComplaintPriority priority;

    @Min(value = 1, message = "Resolution time must be at least 1 hour")
    private int resolutionTimeHours;

    @Min(value = 1, message = "Escalation time must be at least 1 hour")
    private int escalationTimeHours;

    private boolean active;

    public SlaPolicyDto() {
    }

    public static SlaPolicyDto fromEntity(SlaPolicy entity) {
        SlaPolicyDto dto = new SlaPolicyDto();
        dto.setId(entity.getId());
        dto.setOrganizationId(entity.getOrganization().getId());
        if (entity.getDepartment() != null) {
            dto.setDepartmentId(entity.getDepartment().getId());
        }
        dto.setPriority(entity.getPriority());
        dto.setResolutionTimeHours(entity.getResolutionTimeHours());
        dto.setEscalationTimeHours(entity.getEscalationTimeHours());
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

    public ComplaintPriority getPriority() {
        return priority;
    }

    public void setPriority(ComplaintPriority priority) {
        this.priority = priority;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
