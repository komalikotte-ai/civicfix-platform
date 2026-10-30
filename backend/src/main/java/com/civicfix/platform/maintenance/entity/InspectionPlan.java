package com.civicfix.platform.maintenance.entity;

import com.civicfix.platform.assets.enums.AssetType;
import com.civicfix.platform.common.entity.BaseAuditableEntity;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.maintenance.enums.PlanType;
import com.civicfix.platform.maintenance.enums.RecurrenceFrequency;
import com.civicfix.platform.organizations.entity.Organization;
import jakarta.persistence.*;

@Entity
@Table(name = "inspection_plans", indexes = {
        @Index(name = "idx_plan_code", columnList = "plan_code"),
        @Index(name = "idx_plan_type", columnList = "plan_type"),
        @Index(name = "idx_plan_asset_type", columnList = "target_asset_type"),
        @Index(name = "idx_plan_dept", columnList = "department_id")
})
public class InspectionPlan extends BaseAuditableEntity {

    @Column(name = "plan_code", nullable = false, unique = true, length = 50)
    private String planCode;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false, length = 50)
    private PlanType planType = PlanType.ROUTINE;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_asset_type", length = 50)
    private AssetType targetAssetType;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false, length = 30)
    private RecurrenceFrequency frequency = RecurrenceFrequency.MONTHLY;

    @Column(name = "checklist_json", nullable = false, columnDefinition = "TEXT")
    private String checklistJson;

    @Column(name = "safety_guidelines", columnDefinition = "TEXT")
    private String safetyGuidelines;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes = 60;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public InspectionPlan() {
    }

    public String getPlanCode() {
        return planCode;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PlanType getPlanType() {
        return planType;
    }

    public void setPlanType(PlanType planType) {
        this.planType = planType;
    }

    public AssetType getTargetAssetType() {
        return targetAssetType;
    }

    public void setTargetAssetType(AssetType targetAssetType) {
        this.targetAssetType = targetAssetType;
    }

    public RecurrenceFrequency getFrequency() {
        return frequency;
    }

    public void setFrequency(RecurrenceFrequency frequency) {
        this.frequency = frequency;
    }

    public String getChecklistJson() {
        return checklistJson;
    }

    public void setChecklistJson(String checklistJson) {
        this.checklistJson = checklistJson;
    }

    public String getSafetyGuidelines() {
        return safetyGuidelines;
    }

    public void setSafetyGuidelines(String safetyGuidelines) {
        this.safetyGuidelines = safetyGuidelines;
    }

    public Integer getEstimatedDurationMinutes() {
        return estimatedDurationMinutes;
    }

    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) {
        this.estimatedDurationMinutes = estimatedDurationMinutes;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
