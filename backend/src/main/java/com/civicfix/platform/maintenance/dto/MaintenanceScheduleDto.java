package com.civicfix.platform.maintenance.dto;

import com.civicfix.platform.maintenance.entity.MaintenanceSchedule;
import com.civicfix.platform.maintenance.enums.RecurrenceFrequency;
import com.civicfix.platform.maintenance.enums.ScheduleStatus;
import com.civicfix.platform.workorders.enums.WorkOrderPriority;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class MaintenanceScheduleDto {

    private Long id;
    private String scheduleCode;
    private String title;
    private String description;
    private Long assetId;
    private String assetCode;
    private String assetName;
    private Long departmentId;
    private String departmentName;
    private Long inspectionPlanId;
    private String inspectionPlanName;
    private Long assignedWorkerId;
    private String assignedWorkerName;
    private Long assignedSupervisorId;
    private String assignedSupervisorName;
    private RecurrenceFrequency frequency;
    private Integer intervalDays;
    private LocalDate startDate;
    private LocalDate endDate;
    private Instant lastRunDate;
    private Instant nextDueDate;
    private BigDecimal estimatedDurationHours;
    private BigDecimal estimatedCost;
    private WorkOrderPriority priority;
    private ScheduleStatus status;
    private boolean autoGenerateWorkOrder;
    private String checklistJson;
    private boolean active;
    private Instant createdAt;

    public MaintenanceScheduleDto() {}

    public static MaintenanceScheduleDto fromEntity(MaintenanceSchedule schedule) {
        if (schedule == null) return null;
        MaintenanceScheduleDto dto = new MaintenanceScheduleDto();
        dto.setId(schedule.getId());
        dto.setScheduleCode(schedule.getScheduleCode());
        dto.setTitle(schedule.getTitle());
        dto.setDescription(schedule.getDescription());
        if (schedule.getAsset() != null) {
            dto.setAssetId(schedule.getAsset().getId());
            dto.setAssetCode(schedule.getAsset().getAssetCode());
            dto.setAssetName(schedule.getAsset().getName());
        }
        if (schedule.getDepartment() != null) {
            dto.setDepartmentId(schedule.getDepartment().getId());
            dto.setDepartmentName(schedule.getDepartment().getName());
        }
        if (schedule.getInspectionPlan() != null) {
            dto.setInspectionPlanId(schedule.getInspectionPlan().getId());
            dto.setInspectionPlanName(schedule.getInspectionPlan().getName());
        }
        if (schedule.getAssignedWorker() != null) {
            dto.setAssignedWorkerId(schedule.getAssignedWorker().getId());
            dto.setAssignedWorkerName(schedule.getAssignedWorker().getFullName());
        }
        if (schedule.getAssignedSupervisor() != null) {
            dto.setAssignedSupervisorId(schedule.getAssignedSupervisor().getId());
            dto.setAssignedSupervisorName(schedule.getAssignedSupervisor().getFullName());
        }
        dto.setFrequency(schedule.getFrequency());
        dto.setIntervalDays(schedule.getIntervalDays());
        dto.setStartDate(schedule.getStartDate());
        dto.setEndDate(schedule.getEndDate());
        dto.setLastRunDate(schedule.getLastRunDate());
        dto.setNextDueDate(schedule.getNextDueDate());
        dto.setEstimatedDurationHours(schedule.getEstimatedDurationHours());
        dto.setEstimatedCost(schedule.getEstimatedCost());
        dto.setPriority(schedule.getPriority());
        dto.setStatus(schedule.getStatus());
        dto.setAutoGenerateWorkOrder(schedule.isAutoGenerateWorkOrder());
        dto.setChecklistJson(schedule.getChecklistJson());
        dto.setActive(schedule.isActive());
        dto.setCreatedAt(schedule.getCreatedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getScheduleCode() { return scheduleCode; }
    public void setScheduleCode(String scheduleCode) { this.scheduleCode = scheduleCode; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }
    public String getAssetCode() { return assetCode; }
    public void setAssetCode(String assetCode) { this.assetCode = assetCode; }
    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public Long getInspectionPlanId() { return inspectionPlanId; }
    public void setInspectionPlanId(Long inspectionPlanId) { this.inspectionPlanId = inspectionPlanId; }
    public String getInspectionPlanName() { return inspectionPlanName; }
    public void setInspectionPlanName(String inspectionPlanName) { this.inspectionPlanName = inspectionPlanName; }
    public Long getAssignedWorkerId() { return assignedWorkerId; }
    public void setAssignedWorkerId(Long assignedWorkerId) { this.assignedWorkerId = assignedWorkerId; }
    public String getAssignedWorkerName() { return assignedWorkerName; }
    public void setAssignedWorkerName(String assignedWorkerName) { this.assignedWorkerName = assignedWorkerName; }
    public Long getAssignedSupervisorId() { return assignedSupervisorId; }
    public void setAssignedSupervisorId(Long assignedSupervisorId) { this.assignedSupervisorId = assignedSupervisorId; }
    public String getAssignedSupervisorName() { return assignedSupervisorName; }
    public void setAssignedSupervisorName(String assignedSupervisorName) { this.assignedSupervisorName = assignedSupervisorName; }
    public RecurrenceFrequency getFrequency() { return frequency; }
    public void setFrequency(RecurrenceFrequency frequency) { this.frequency = frequency; }
    public Integer getIntervalDays() { return intervalDays; }
    public void setIntervalDays(Integer intervalDays) { this.intervalDays = intervalDays; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Instant getLastRunDate() { return lastRunDate; }
    public void setLastRunDate(Instant lastRunDate) { this.lastRunDate = lastRunDate; }
    public Instant getNextDueDate() { return nextDueDate; }
    public void setNextDueDate(Instant nextDueDate) { this.nextDueDate = nextDueDate; }
    public BigDecimal getEstimatedDurationHours() { return estimatedDurationHours; }
    public void setEstimatedDurationHours(BigDecimal estimatedDurationHours) { this.estimatedDurationHours = estimatedDurationHours; }
    public BigDecimal getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(BigDecimal estimatedCost) { this.estimatedCost = estimatedCost; }
    public WorkOrderPriority getPriority() { return priority; }
    public void setPriority(WorkOrderPriority priority) { this.priority = priority; }
    public ScheduleStatus getStatus() { return status; }
    public void setStatus(ScheduleStatus status) { this.status = status; }
    public boolean isAutoGenerateWorkOrder() { return autoGenerateWorkOrder; }
    public void setAutoGenerateWorkOrder(boolean autoGenerateWorkOrder) { this.autoGenerateWorkOrder = autoGenerateWorkOrder; }
    public String getChecklistJson() { return checklistJson; }
    public void setChecklistJson(String checklistJson) { this.checklistJson = checklistJson; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
