package com.civicfix.platform.sla.dto;

import com.civicfix.platform.sla.entity.ComplaintEscalationHistory;
import com.civicfix.platform.sla.enums.EscalationLevel;

import java.time.Instant;

public class ComplaintEscalationHistoryDto {

    private Long id;
    private Long complaintId;
    private String trackingNumber;
    private EscalationLevel fromLevel;
    private EscalationLevel toLevel;
    private Long escalatedToEmployeeId;
    private String escalatedToEmployeeName;
    private String targetRole;
    private String reason;
    private String actionTaken;
    private Instant escalatedAt;

    public ComplaintEscalationHistoryDto() {
    }

    public static ComplaintEscalationHistoryDto fromEntity(ComplaintEscalationHistory entity) {
        if (entity == null) return null;
        ComplaintEscalationHistoryDto dto = new ComplaintEscalationHistoryDto();
        dto.setId(entity.getId());
        if (entity.getComplaint() != null) {
            dto.setComplaintId(entity.getComplaint().getId());
            dto.setTrackingNumber(entity.getComplaint().getTrackingNumber());
        }
        dto.setFromLevel(entity.getFromLevel());
        dto.setToLevel(entity.getToLevel());
        if (entity.getEscalatedToEmployee() != null) {
            dto.setEscalatedToEmployeeId(entity.getEscalatedToEmployee().getId());
            dto.setEscalatedToEmployeeName(entity.getEscalatedToEmployee().getFullName());
        }
        dto.setTargetRole(entity.getTargetRole());
        dto.setReason(entity.getReason());
        dto.setActionTaken(entity.getActionTaken());
        dto.setEscalatedAt(entity.getEscalatedAt());
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

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public EscalationLevel getFromLevel() {
        return fromLevel;
    }

    public void setFromLevel(EscalationLevel fromLevel) {
        this.fromLevel = fromLevel;
    }

    public EscalationLevel getToLevel() {
        return toLevel;
    }

    public void setToLevel(EscalationLevel toLevel) {
        this.toLevel = toLevel;
    }

    public Long getEscalatedToEmployeeId() {
        return escalatedToEmployeeId;
    }

    public void setEscalatedToEmployeeId(Long escalatedToEmployeeId) {
        this.escalatedToEmployeeId = escalatedToEmployeeId;
    }

    public String getEscalatedToEmployeeName() {
        return escalatedToEmployeeName;
    }

    public void setEscalatedToEmployeeName(String escalatedToEmployeeName) {
        this.escalatedToEmployeeName = escalatedToEmployeeName;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }

    public Instant getEscalatedAt() {
        return escalatedAt;
    }

    public void setEscalatedAt(Instant escalatedAt) {
        this.escalatedAt = escalatedAt;
    }
}
