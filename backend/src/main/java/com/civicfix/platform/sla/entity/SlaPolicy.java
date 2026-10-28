package com.civicfix.platform.sla.entity;

import com.civicfix.platform.common.entity.BaseAuditableEntity;
import com.civicfix.platform.complaints.enums.ComplaintPriority;
import com.civicfix.platform.complaints.enums.ComplaintSeverity;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.organizations.entity.Organization;
import jakarta.persistence.*;

@Entity
@Table(name = "sla_policies", indexes = {
        @Index(name = "idx_sla_org_dept", columnList = "organization_id, department_id"),
        @Index(name = "idx_sla_priority", columnList = "priority")
})
public class SlaPolicy extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "category", length = 100)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private ComplaintPriority priority = ComplaintPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 20)
    private ComplaintSeverity severity;

    @Column(name = "response_time_hours", nullable = false)
    private int responseTimeHours = 2;

    @Column(name = "resolution_time_hours", nullable = false)
    private int resolutionTimeHours = 48;

    @Column(name = "escalation_time_hours", nullable = false)
    private int escalationTimeHours = 12;

    @Column(name = "warning_threshold_percent", nullable = false)
    private int warningThresholdPercent = 75;

    @Column(name = "business_hours_only", nullable = false)
    private boolean businessHoursOnly = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_id")
    private BusinessCalendar calendar;

    public SlaPolicy() {
    }

    public SlaPolicy(Organization organization, Department department, ComplaintPriority priority,
                     int resolutionTimeHours, int escalationTimeHours) {
        this.organization = organization;
        this.department = department;
        this.priority = priority;
        this.resolutionTimeHours = resolutionTimeHours;
        this.escalationTimeHours = escalationTimeHours;
    }

    public SlaPolicy(Organization organization, Department department, String category,
                     ComplaintPriority priority, ComplaintSeverity severity,
                     int responseTimeHours, int resolutionTimeHours, int escalationTimeHours,
                     int warningThresholdPercent, boolean businessHoursOnly, BusinessCalendar calendar) {
        this.organization = organization;
        this.department = department;
        this.category = category;
        this.priority = priority != null ? priority : ComplaintPriority.MEDIUM;
        this.severity = severity;
        this.responseTimeHours = responseTimeHours;
        this.resolutionTimeHours = resolutionTimeHours;
        this.escalationTimeHours = escalationTimeHours;
        this.warningThresholdPercent = warningThresholdPercent;
        this.businessHoursOnly = businessHoursOnly;
        this.calendar = calendar;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
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

    public BusinessCalendar getCalendar() {
        return calendar;
    }

    public void setCalendar(BusinessCalendar calendar) {
        this.calendar = calendar;
    }
}
