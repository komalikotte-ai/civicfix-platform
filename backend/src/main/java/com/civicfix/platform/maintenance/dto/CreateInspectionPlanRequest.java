package com.civicfix.platform.maintenance.dto;

import com.civicfix.platform.assets.enums.AssetType;
import com.civicfix.platform.maintenance.enums.PlanType;
import com.civicfix.platform.maintenance.enums.RecurrenceFrequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateInspectionPlanRequest {

    private String planCode;

    @NotBlank(message = "Plan name is required")
    private String name;

    private String description;

    private PlanType planType = PlanType.ROUTINE;

    private AssetType targetAssetType;

    private RecurrenceFrequency frequency = RecurrenceFrequency.MONTHLY;

    @NotBlank(message = "Checklist JSON is required")
    private String checklistJson;

    private String safetyGuidelines;

    private Integer estimatedDurationMinutes = 60;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotNull(message = "Organization ID is required")
    private Long organizationId;

    public CreateInspectionPlanRequest() {}

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
    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }
}
