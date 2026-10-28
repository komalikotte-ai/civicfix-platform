package com.civicfix.platform.sla.entity;

import com.civicfix.platform.common.entity.BaseAuditableEntity;
import com.civicfix.platform.complaints.enums.ComplaintPriority;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.organizations.entity.Organization;
import jakarta.persistence.*;

@Entity
@Table(name = "sla_policies")
public class SlaPolicy extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private ComplaintPriority priority;

    @Column(name = "resolution_time_hours", nullable = false)
    private int resolutionTimeHours;

    @Column(name = "escalation_time_hours", nullable = false)
    private int escalationTimeHours;

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
}
