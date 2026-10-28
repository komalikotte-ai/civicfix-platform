package com.civicfix.platform.complaints.dto;

import jakarta.validation.constraints.Size;

public class AssignComplaintRequest {

    private Long departmentId;
    private Long assignedEmployeeId;
    private Long assignedSupervisorId;

    @Size(max = 500, message = "Assignment reason cannot exceed 500 characters")
    private String reason;

    public AssignComplaintRequest() {
    }

    public AssignComplaintRequest(Long departmentId, Long assignedEmployeeId, Long assignedSupervisorId, String reason) {
        this.departmentId = departmentId;
        this.assignedEmployeeId = assignedEmployeeId;
        this.assignedSupervisorId = assignedSupervisorId;
        this.reason = reason;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Long getAssignedEmployeeId() {
        return assignedEmployeeId;
    }

    public void setAssignedEmployeeId(Long assignedEmployeeId) {
        this.assignedEmployeeId = assignedEmployeeId;
    }

    public Long getAssignedSupervisorId() {
        return assignedSupervisorId;
    }

    public void setAssignedSupervisorId(Long assignedSupervisorId) {
        this.assignedSupervisorId = assignedSupervisorId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
