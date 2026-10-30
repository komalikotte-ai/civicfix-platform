package com.civicfix.platform.maintenance.dto;

import com.civicfix.platform.assets.enums.AssetType;
import com.civicfix.platform.maintenance.entity.InspectionPlan;
import com.civicfix.platform.maintenance.enums.PlanType;
import com.civicfix.platform.maintenance.enums.RecurrenceFrequency;

import java.time.Instant;

public class InspectionPlanDto {

    private Long id;
    private String planCode;
    private String name;
    private String description;
    private PlanType planType;
    private AssetType targetAssetType;
    private RecurrenceFrequency frequency;
    private String checklistJson;
    private String safetyGuidelines;
    private Integer estimatedDurationMinutes;
    private Long departmentId;
    private String departmentName;
    private Long organizationId;
    private boolean active;
    private Instant createdAt;

    public InspectionPlanDto() {}

    public static InspectionPlanDto fromEntity(InspectionPlan plan) {
        if (plan == null) return null;
        InspectionPlanDto dto = new InspectionPlanDto();
        dto.setId(plan.getId());
        dto.setPlanCode(plan.getPlanCode());
        dto.setName(plan.getName());
        dto.setDescription(plan.getDescription());
        dto.setPlanType(plan.getPlanType());
        dto.setTargetAssetType(plan.getTargetAssetType());
        dto.setFrequency(plan.getFrequency());
        dto.setChecklistJson(plan.getChecklistJson());
        dto.setSafetyGuidelines(plan.getSafetyGuidelines());
        dto.setEstimatedDurationMinutes(plan.getEstimatedDurationMinutes());
        if (plan.getDepartment() != null) {
            dto.setDepartmentId(plan.getDepartment().getId());
            dto.setDepartmentName(plan.getDepartment().getName());
        }
        if (plan.getOrganization() != null) {
            dto.setOrganizationId(plan.getOrganization().getId());
        }
        dto.setActive(plan.isActive());
        dto.setCreatedAt(plan.getCreatedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPlanCode() { return planCode; }
    public void setPlanCode(String planCode) { this.planCode = planCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public PlanType getPlanType() { return planType; }
    public void setPlanType(PlanType planType) { this.planType = planType; }
    public AssetType getTargetAssetType() { return targetAssetType; }
    public void setTargetAssetType(AssetType targetAssetType) { this.targetAssetType = targetAssetType; }
    public RecurrenceFrequency getFrequency() { return frequency; }
    public void setFrequency(RecurrenceFrequency frequency) { this.frequency = frequency; }
    public String getChecklistJson() { return checklistJson; }
    public void setChecklistJson(String checklistJson) { this.checklistJson = checklistJson; }
    public String getSafetyGuidelines() { return safetyGuidelines; }
    public void setSafetyGuidelines(String safetyGuidelines) { this.safetyGuidelines = safetyGuidelines; }
    public Integer getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
