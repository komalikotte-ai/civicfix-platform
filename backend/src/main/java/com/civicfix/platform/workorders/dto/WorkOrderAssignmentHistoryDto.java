package com.civicfix.platform.workorders.dto;

import com.civicfix.platform.workorders.entity.WorkOrderAssignmentHistory;

import java.time.Instant;

public class WorkOrderAssignmentHistoryDto {

    private Long id;
    private Long workOrderId;
    private Long previousWorkerId;
    private String previousWorkerName;
    private Long newWorkerId;
    private String newWorkerName;
    private Long previousSupervisorId;
    private String previousSupervisorName;
    private Long newSupervisorId;
    private String newSupervisorName;
    private String assignedBy;
    private String reason;
    private Instant assignedAt;

    public WorkOrderAssignmentHistoryDto() {
    }

    public static WorkOrderAssignmentHistoryDto fromEntity(WorkOrderAssignmentHistory entity) {
        if (entity == null) {
            return null;
        }

        WorkOrderAssignmentHistoryDto dto = new WorkOrderAssignmentHistoryDto();
        dto.setId(entity.getId());
        if (entity.getWorkOrder() != null) {
            dto.setWorkOrderId(entity.getWorkOrder().getId());
        }
        if (entity.getPreviousWorker() != null) {
            dto.setPreviousWorkerId(entity.getPreviousWorker().getId());
            dto.setPreviousWorkerName(entity.getPreviousWorker().getFullName());
        }
        if (entity.getNewWorker() != null) {
            dto.setNewWorkerId(entity.getNewWorker().getId());
            dto.setNewWorkerName(entity.getNewWorker().getFullName());
        }
        if (entity.getPreviousSupervisor() != null) {
            dto.setPreviousSupervisorId(entity.getPreviousSupervisor().getId());
            dto.setPreviousSupervisorName(entity.getPreviousSupervisor().getFullName());
        }
        if (entity.getNewSupervisor() != null) {
            dto.setNewSupervisorId(entity.getNewSupervisor().getId());
            dto.setNewSupervisorName(entity.getNewSupervisor().getFullName());
        }
        dto.setAssignedBy(entity.getAssignedBy());
        dto.setReason(entity.getReason());
        dto.setAssignedAt(entity.getAssignedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    public Long getPreviousWorkerId() {
        return previousWorkerId;
    }

    public void setPreviousWorkerId(Long previousWorkerId) {
        this.previousWorkerId = previousWorkerId;
    }

    public String getPreviousWorkerName() {
        return previousWorkerName;
    }

    public void setPreviousWorkerName(String previousWorkerName) {
        this.previousWorkerName = previousWorkerName;
    }

    public Long getNewWorkerId() {
        return newWorkerId;
    }

    public void setNewWorkerId(Long newWorkerId) {
        this.newWorkerId = newWorkerId;
    }

    public String getNewWorkerName() {
        return newWorkerName;
    }

    public void setNewWorkerName(String newWorkerName) {
        this.newWorkerName = newWorkerName;
    }

    public Long getPreviousSupervisorId() {
        return previousSupervisorId;
    }

    public void setPreviousSupervisorId(Long previousSupervisorId) {
        this.previousSupervisorId = previousSupervisorId;
    }

    public String getPreviousSupervisorName() {
        return previousSupervisorName;
    }

    public void setPreviousSupervisorName(String previousSupervisorName) {
        this.previousSupervisorName = previousSupervisorName;
    }

    public Long getNewSupervisorId() {
        return newSupervisorId;
    }

    public void setNewSupervisorId(Long newSupervisorId) {
        this.newSupervisorId = newSupervisorId;
    }

    public String getNewSupervisorName() {
        return newSupervisorName;
    }

    public void setNewSupervisorName(String newSupervisorName) {
        this.newSupervisorName = newSupervisorName;
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Instant assignedAt) {
        this.assignedAt = assignedAt;
    }
}
