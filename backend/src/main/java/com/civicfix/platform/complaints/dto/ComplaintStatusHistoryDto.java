package com.civicfix.platform.complaints.dto;

import com.civicfix.platform.complaints.entity.ComplaintStatusHistory;
import com.civicfix.platform.complaints.enums.ComplaintStatus;

import java.time.Instant;

public class ComplaintStatusHistoryDto {

    private Long id;
    private Long complaintId;
    private ComplaintStatus fromStatus;
    private ComplaintStatus toStatus;
    private Long actionByUserId;
    private String actionByUsername;
    private String remarks;
    private Instant transitionTime;

    public ComplaintStatusHistoryDto() {
    }

    public static ComplaintStatusHistoryDto fromEntity(ComplaintStatusHistory h) {
        if (h == null) return null;
        ComplaintStatusHistoryDto dto = new ComplaintStatusHistoryDto();
        dto.setId(h.getId());
        if (h.getComplaint() != null) {
            dto.setComplaintId(h.getComplaint().getId());
        }
        dto.setFromStatus(h.getFromStatus());
        dto.setToStatus(h.getToStatus());
        if (h.getActionByUser() != null) {
            dto.setActionByUserId(h.getActionByUser().getId());
        }
        dto.setActionByUsername(h.getActionByUsername());
        dto.setRemarks(h.getRemarks());
        dto.setTransitionTime(h.getTransitionTime());
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

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Instant getTransitionTime() {
        return transitionTime;
    }

    public void setTransitionTime(Instant transitionTime) {
        this.transitionTime = transitionTime;
    }
}
