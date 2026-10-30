package com.civicfix.platform.maintenance.entity;

import com.civicfix.platform.assets.entity.Asset;
import com.civicfix.platform.common.entity.BaseAuditableEntity;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.maintenance.enums.RecurrenceFrequency;
import com.civicfix.platform.maintenance.enums.ScheduleStatus;
import com.civicfix.platform.users.entity.Employee;
import com.civicfix.platform.workorders.enums.WorkOrderPriority;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "maintenance_schedules", indexes = {
        @Index(name = "idx_sched_code", columnList = "schedule_code"),
        @Index(name = "idx_sched_asset", columnList = "asset_id"),
        @Index(name = "idx_sched_dept", columnList = "department_id"),
        @Index(name = "idx_sched_next_due", columnList = "next_due_date"),
        @Index(name = "idx_sched_status", columnList = "status")
})
public class MaintenanceSchedule extends BaseAuditableEntity {

    @Column(name = "schedule_code", nullable = false, unique = true, length = 50)
    private String scheduleCode;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_plan_id")
    private InspectionPlan inspectionPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_worker_id")
    private Employee assignedWorker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_supervisor_id")
    private Employee assignedSupervisor;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false, length = 30)
    private RecurrenceFrequency frequency = RecurrenceFrequency.MONTHLY;

    @Column(name = "interval_days")
    private Integer intervalDays = 30;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "last_run_date")
    private Instant lastRunDate;

    @Column(name = "next_due_date", nullable = false)
    private Instant nextDueDate;

    @Column(name = "estimated_duration_hours", precision = 6, scale = 2)
    private BigDecimal estimatedDurationHours = new BigDecimal("2.00");

    @Column(name = "estimated_cost", precision = 12, scale = 2)
    private BigDecimal estimatedCost = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 30)
    private WorkOrderPriority priority = WorkOrderPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ScheduleStatus status = ScheduleStatus.ACTIVE;

    @Column(name = "auto_generate_work_order", nullable = false)
    private boolean autoGenerateWorkOrder = true;

    @Column(name = "checklist_json", columnDefinition = "TEXT")
    private String checklistJson;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public MaintenanceSchedule() {}

    public String getScheduleCode() { return scheduleCode; }
    public void setScheduleCode(String scheduleCode) { this.scheduleCode = scheduleCode; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Asset getAsset() { return asset; }
    public void setAsset(Asset asset) { this.asset = asset; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public InspectionPlan getInspectionPlan() { return inspectionPlan; }
    public void setInspectionPlan(InspectionPlan inspectionPlan) { this.inspectionPlan = inspectionPlan; }
    public Employee getAssignedWorker() { return assignedWorker; }
    public void setAssignedWorker(Employee assignedWorker) { this.assignedWorker = assignedWorker; }
    public Employee getAssignedSupervisor() { return assignedSupervisor; }
    public void setAssignedSupervisor(Employee assignedSupervisor) { this.assignedSupervisor = assignedSupervisor; }
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
}
