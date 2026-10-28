package com.civicfix.platform.users.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AssignEmployeeRequest {

    @NotNull(message = "New department ID is required")
    private Long newDepartmentId;

    private Long newTeamId;

    private Long newSupervisorId;

    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;

    public AssignEmployeeRequest() {
    }

    public AssignEmployeeRequest(Long newDepartmentId, Long newTeamId, Long newSupervisorId, String reason) {
        this.newDepartmentId = newDepartmentId;
        this.newTeamId = newTeamId;
        this.newSupervisorId = newSupervisorId;
        this.reason = reason;
    }

    public Long getNewDepartmentId() {
        return newDepartmentId;
    }

    public void setNewDepartmentId(Long newDepartmentId) {
        this.newDepartmentId = newDepartmentId;
    }

    public Long getNewTeamId() {
        return newTeamId;
    }

    public void setNewTeamId(Long newTeamId) {
        this.newTeamId = newTeamId;
    }

    public Long getNewSupervisorId() {
        return newSupervisorId;
    }

    public void setNewSupervisorId(Long newSupervisorId) {
        this.newSupervisorId = newSupervisorId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
