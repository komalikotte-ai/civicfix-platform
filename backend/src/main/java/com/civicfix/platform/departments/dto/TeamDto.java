package com.civicfix.platform.departments.dto;

import com.civicfix.platform.departments.entity.Team;

public class TeamDto {

    private Long id;
    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private String name;
    private String code;
    private String description;
    private Long supervisorId;
    private String supervisorName;
    private String supervisorCode;
    private boolean active;

    public TeamDto() {
    }

    public static TeamDto fromEntity(Team team) {
        if (team == null) return null;
        TeamDto dto = new TeamDto();
        dto.setId(team.getId());
        if (team.getDepartment() != null) {
            dto.setDepartmentId(team.getDepartment().getId());
            dto.setDepartmentName(team.getDepartment().getName());
            dto.setDepartmentCode(team.getDepartment().getCode());
        }
        dto.setName(team.getName());
        dto.setCode(team.getCode());
        dto.setDescription(team.getDescription());
        if (team.getSupervisor() != null) {
            dto.setSupervisorId(team.getSupervisor().getId());
            dto.setSupervisorName(team.getSupervisor().getFullName());
            dto.setSupervisorCode(team.getSupervisor().getEmployeeCode());
        }
        dto.setActive(team.isActive());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getSupervisorId() {
        return supervisorId;
    }

    public void setSupervisorId(Long supervisorId) {
        this.supervisorId = supervisorId;
    }

    public String getSupervisorName() {
        return supervisorName;
    }

    public void setSupervisorName(String supervisorName) {
        this.supervisorName = supervisorName;
    }

    public String getSupervisorCode() {
        return supervisorCode;
    }

    public void setSupervisorCode(String supervisorCode) {
        this.supervisorCode = supervisorCode;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
