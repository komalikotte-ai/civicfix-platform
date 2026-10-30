package com.civicfix.platform.maintenance.dto;

import com.civicfix.platform.maintenance.enums.RecurrenceFrequency;
import com.civicfix.platform.workorders.enums.WorkOrderPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class CreateMaintenanceScheduleRequest {

    private String scheduleCode;

    @NotBlank(message = "Schedule title is required")
    private String title;

    private String description;

    @NotNull(message = "Asset ID is required")
    private Long assetId;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    private Long inspectionPlanId;
    private Long assignedWorkerId;
    private Long assignedSupervisorId;

    private RecurrenceFrequency frequency = RecurrenceFrequency.MONTHLY;
    private Integer intervalDays = 30;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    private Instant nextDueDate;

    private BigDecimal estimatedDurationHours = new BigDecimal("2.00");
    private BigDecimal estimatedCost = BigDecimal.ZERO;
    private WorkOrderPriority priority = WorkOrderPriority.MEDIUM;
    private boolean autoGenerateWorkOrder = true;
    private String checklistJson;

    public CreateMaintenanceScheduleRequest() {}

    public String getScheduleCode() { return scheduleCode; }
    public void setScheduleCode(String scheduleCode) { this.scheduleCode = scheduleCode; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public Long getInspectionPlanId() { return inspectionPlanId; }
    public void setInspectionPlanId(Long inspectionPlanId) { this.inspectionPlanId = inspectionPlanId; }
    public Long getAssignedWorkerId() { return assignedWorkerId; }
    public void setAssignedWorkerId(Long assignedWorkerId) { this.assignedWorkerId = assignedWorkerId; }
    public Long getAssignedSupervisorId() { return assignedSupervisorId; }
    public void setAssignedSupervisorId(Long assignedSupervisorId) { this.assignedSupervisorId = assignedSupervisorId; }
    public RecurrenceFrequency getFrequency() { return frequency; }
    public void setFrequency(RecurrenceFrequency frequency) { this.frequency = frequency; }
    public Integer getIntervalDays() { return intervalDays; }
    public void setIntervalDays(Integer intervalDays) { this.intervalDays = intervalDays; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Instant getNextDueDate() { return nextDueDate; }
    public void setNextDueDate(Instant nextDueDate) { this.nextDueDate = nextDueDate; }
    public BigDecimal getEstimatedDurationHours() { return estimatedDurationHours; }
    public void setEstimatedDurationHours(BigDecimal estimatedDurationHours) { this.estimatedDurationHours = estimatedDurationHours; }
    public BigDecimal getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(BigDecimal estimatedCost) { this.estimatedCost = estimatedCost; }
    public WorkOrderPriority getPriority() { return priority; }
    public void setPriority(WorkOrderPriority priority) { this.priority = priority; }
    public boolean isAutoGenerateWorkOrder() { return autoGenerateWorkOrder; }
    public void setAutoGenerateWorkOrder(boolean autoGenerateWorkOrder) { this.autoGenerateWorkOrder = autoGenerateWorkOrder; }
    public String getChecklistJson() { return checklistJson; }
    public void setChecklistJson(String checklistJson) { this.checklistJson = checklistJson; }
}
