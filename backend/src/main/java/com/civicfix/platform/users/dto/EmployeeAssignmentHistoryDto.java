package com.civicfix.platform.users.dto;

import com.civicfix.platform.users.entity.EmployeeAssignmentHistory;

import java.time.Instant;

public class EmployeeAssignmentHistoryDto {

    private Long id;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;

    private Long previousDepartmentId;
    private String previousDepartmentName;
    private Long newDepartmentId;
    private String newDepartmentName;

    private Long previousTeamId;
    private String previousTeamName;
    private Long newTeamId;
    private String newTeamName;

    private Long previousSupervisorId;
    private String previousSupervisorName;
    private Long newSupervisorId;
    private String newSupervisorName;

    private Instant effectiveFrom;
    private Instant effectiveTo;
    private String assignedBy;
    private String reason;

    public EmployeeAssignmentHistoryDto() {
    }

    public static EmployeeAssignmentHistoryDto fromEntity(EmployeeAssignmentHistory h) {
        if (h == null) return null;
        EmployeeAssignmentHistoryDto dto = new EmployeeAssignmentHistoryDto();
        dto.setId(h.getId());
        if (h.getEmployee() != null) {
            dto.setEmployeeId(h.getEmployee().getId());
            dto.setEmployeeCode(h.getEmployee().getEmployeeCode());
            dto.setEmployeeName(h.getEmployee().getFullName());
        }
        if (h.getPreviousDepartment() != null) {
            dto.setPreviousDepartmentId(h.getPreviousDepartment().getId());
            dto.setPreviousDepartmentName(h.getPreviousDepartment().getName());
        }
        if (h.getNewDepartment() != null) {
            dto.setNewDepartmentId(h.getNewDepartment().getId());
            dto.setNewDepartmentName(h.getNewDepartment().getName());
        }
        if (h.getPreviousTeam() != null) {
            dto.setPreviousTeamId(h.getPreviousTeam().getId());
            dto.setPreviousTeamName(h.getPreviousTeam().getName());
        }
        if (h.getNewTeam() != null) {
            dto.setNewTeamId(h.getNewTeam().getId());
            dto.setNewTeamName(h.getNewTeam().getName());
        }
        if (h.getPreviousSupervisor() != null) {
            dto.setPreviousSupervisorId(h.getPreviousSupervisor().getId());
            dto.setPreviousSupervisorName(h.getPreviousSupervisor().getFullName());
        }
        if (h.getNewSupervisor() != null) {
            dto.setNewSupervisorId(h.getNewSupervisor().getId());
            dto.setNewSupervisorName(h.getNewSupervisor().getFullName());
        }
        dto.setEffectiveFrom(h.getEffectiveFrom());
        dto.setEffectiveTo(h.getEffectiveTo());
        dto.setAssignedBy(h.getAssignedBy());
        dto.setReason(h.getReason());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
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

    public Long getPreviousTeamId() {
        return previousTeamId;
    }

    public void setPreviousTeamId(Long previousTeamId) {
        this.previousTeamId = previousTeamId;
    }

    public String getPreviousTeamName() {
        return previousTeamName;
    }

    public void setPreviousTeamName(String previousTeamName) {
        this.previousTeamName = previousTeamName;
    }

    public Long getNewTeamId() {
        return newTeamId;
    }

    public void setNewTeamId(Long newTeamId) {
        this.newTeamId = newTeamId;
    }

    public String getNewTeamName() {
        return newTeamName;
    }

    public void setNewTeamName(String newTeamName) {
        this.newTeamName = newTeamName;
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

    public Instant getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(Instant effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public Instant getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(Instant effectiveTo) {
        this.effectiveTo = effectiveTo;
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
}
