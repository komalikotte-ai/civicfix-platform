package com.civicfix.platform.complaints.dto;

import com.civicfix.platform.complaints.entity.ComplaintAssignmentHistory;

import java.time.Instant;

public class ComplaintAssignmentHistoryDto {

    private Long id;
    private Long complaintId;
    private Long previousDepartmentId;
    private String previousDepartmentName;
    private Long newDepartmentId;
    private String newDepartmentName;
    private Long previousAssigneeId;
    private String previousAssigneeName;
    private Long newAssigneeId;
    private String newAssigneeName;
    private Long previousSupervisorId;
    private String previousSupervisorName;
    private Long newSupervisorId;
    private String newSupervisorName;
    private String assignedBy;
    private String reason;
    private Instant assignedAt;

    public ComplaintAssignmentHistoryDto() {
    }

    public static ComplaintAssignmentHistoryDto fromEntity(ComplaintAssignmentHistory h) {
        if (h == null) return null;
        ComplaintAssignmentHistoryDto dto = new ComplaintAssignmentHistoryDto();
        dto.setId(h.getId());
        if (h.getComplaint() != null) {
            dto.setComplaintId(h.getComplaint().getId());
        }
        if (h.getPreviousDepartment() != null) {
            dto.setPreviousDepartmentId(h.getPreviousDepartment().getId());
            dto.setPreviousDepartmentName(h.getPreviousDepartment().getName());
        }
        if (h.getNewDepartment() != null) {
            dto.setNewDepartmentId(h.getNewDepartment().getId());
            dto.setNewDepartmentName(h.getNewDepartment().getName());
        }
        if (h.getPreviousAssignee() != null) {
            dto.setPreviousAssigneeId(h.getPreviousAssignee().getId());
            dto.setPreviousAssigneeName(h.getPreviousAssignee().getFullName());
        }
        if (h.getNewAssignee() != null) {
            dto.setNewAssigneeId(h.getNewAssignee().getId());
            dto.setNewAssigneeName(h.getNewAssignee().getFullName());
        }
        if (h.getPreviousSupervisor() != null) {
            dto.setPreviousSupervisorId(h.getPreviousSupervisor().getId());
            dto.setPreviousSupervisorName(h.getPreviousSupervisor().getFullName());
        }
        if (h.getNewSupervisor() != null) {
            dto.setNewSupervisorId(h.getNewSupervisor().getId());
            dto.setNewSupervisorName(h.getNewSupervisor().getFullName());
        }
        dto.setAssignedBy(h.getAssignedBy());
        dto.setReason(h.getReason());
        dto.setAssignedAt(h.getAssignedAt());
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

    public Long getPreviousDepartmentId() {
        return previousDepartmentId;
    }

    public void setPreviousDepartmentId(Long previousDepartmentId) {
        this.previousDepartmentId = previousDepartmentId;
    }

    public String getPreviousDepartmentName() {
        return previousDepartmentName;
    }

    public void setPreviousDepartmentName(String previousDepartmentName) {
        this.previousDepartmentName = previousDepartmentName;
    }

    public Long getNewDepartmentId() {
        return newDepartmentId;
    }

    public void setNewDepartmentId(Long newDepartmentId) {
        this.newDepartmentId = newDepartmentId;
    }

    public String getNewDepartmentName() {
        return newDepartmentName;
    }

    public void setNewDepartmentName(String newDepartmentName) {
        this.newDepartmentName = newDepartmentName;
    }

    public Long getPreviousAssigneeId() {
        return previousAssigneeId;
    }

    public void setPreviousAssigneeId(Long previousAssigneeId) {
        this.previousAssigneeId = previousAssigneeId;
    }

    public String getPreviousAssigneeName() {
        return previousAssigneeName;
    }

    public void setPreviousAssigneeName(String previousAssigneeName) {
        this.previousAssigneeName = previousAssigneeName;
    }

    public Long getNewAssigneeId() {
        return newAssigneeId;
    }

    public void setNewAssigneeId(Long newAssigneeId) {
        this.newAssigneeId = newAssigneeId;
    }

    public String getNewAssigneeName() {
        return newAssigneeName;
    }

    public void setNewAssigneeName(String newAssigneeName) {
        this.newAssigneeName = newAssigneeName;
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
