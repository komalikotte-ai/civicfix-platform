package com.civicfix.platform.complaints.dto;

import com.civicfix.platform.complaints.enums.ComplaintStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateComplaintStatusRequest {

    @NotNull(message = "Target status is required")
    private ComplaintStatus targetStatus;

    private String remarks;
    private Long assignedOfficerId;

    public UpdateComplaintStatusRequest() {
    }

    public ComplaintStatus getTargetStatus() {
        return targetStatus;
    }

    public void setTargetStatus(ComplaintStatus targetStatus) {
        this.targetStatus = targetStatus;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Long getAssignedOfficerId() {
        return assignedOfficerId;
    }

    public void setAssignedOfficerId(Long assignedOfficerId) {
        this.assignedOfficerId = assignedOfficerId;
    }
}
