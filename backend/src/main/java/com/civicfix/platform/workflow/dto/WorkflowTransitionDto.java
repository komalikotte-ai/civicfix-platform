package com.civicfix.platform.workflow.dto;

import com.civicfix.platform.complaints.enums.ComplaintStatus;
import com.civicfix.platform.workflow.entity.WorkflowTransition;

import java.time.Instant;

public class WorkflowTransitionDto {

    private Long id;
    private Long complaintId;
    private ComplaintStatus fromStatus;
    private ComplaintStatus toStatus;
    private String remarks;
    private Long actionByUserId;
    private String actionByUsername;
    private Instant createdAt;

    public WorkflowTransitionDto() {
    }

    public static WorkflowTransitionDto fromEntity(WorkflowTransition entity) {
        WorkflowTransitionDto dto = new WorkflowTransitionDto();
        dto.setId(entity.getId());
        dto.setComplaintId(entity.getComplaint().getId());
        dto.setFromStatus(entity.getFromStatus());
        dto.setToStatus(entity.getToStatus());
        dto.setRemarks(entity.getRemarks());
        dto.setActionByUserId(entity.getActionByUser().getId());
        dto.setActionByUsername(entity.getActionByUser().getUsername());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(Long complaintId) {
        this.complaintId = complaintId;
    }

    public ComplaintStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(ComplaintStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public ComplaintStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(ComplaintStatus toStatus) {
        this.toStatus = toStatus;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Long getActionByUserId() {
        return actionByUserId;
    }

    public void setActionByUserId(Long actionByUserId) {
        this.actionByUserId = actionByUserId;
    }

    public String getActionByUsername() {
        return actionByUsername;
    }

    public void setActionByUsername(String actionByUsername) {
        this.actionByUsername = actionByUsername;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
