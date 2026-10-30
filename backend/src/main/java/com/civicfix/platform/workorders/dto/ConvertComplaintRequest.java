package com.civicfix.platform.workorders.dto;

import com.civicfix.platform.workorders.enums.WorkOrderPriority;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public class ConvertComplaintRequest {

    @NotNull(message = "Complaint ID is required")
    private Long complaintId;

    private Long departmentId;

    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    private String description;

    private WorkOrderPriority priority;

    private Instant scheduledStartDate;

    private Instant expectedCompletionDate;

    private BigDecimal estimatedCost;

    private Long assignedWorkerId;

    private Long assignedSupervisorId;

    public ConvertComplaintRequest() {
    }

    public Long getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(Long complaintId) {
        this.complaintId = complaintId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public WorkOrderPriority getPriority() {
        return priority;
    }

    public void setPriority(WorkOrderPriority priority) {
        this.priority = priority;
    }

    public Instant getScheduledStartDate() {
        return scheduledStartDate;
    }

    public void setScheduledStartDate(Instant scheduledStartDate) {
        this.scheduledStartDate = scheduledStartDate;
    }

    public Instant getExpectedCompletionDate() {
        return expectedCompletionDate;
    }

    public void setExpectedCompletionDate(Instant expectedCompletionDate) {
        this.expectedCompletionDate = expectedCompletionDate;
    }

    public BigDecimal getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(BigDecimal estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public Long getAssignedWorkerId() {
        return assignedWorkerId;
    }

    public void setAssignedWorkerId(Long assignedWorkerId) {
        this.assignedWorkerId = assignedWorkerId;
    }

    public Long getAssignedSupervisorId() {
        return assignedSupervisorId;
    }

    public void setAssignedSupervisorId(Long assignedSupervisorId) {
        this.assignedSupervisorId = assignedSupervisorId;
    }
}
