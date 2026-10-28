package com.civicfix.platform.users.dto;

import com.civicfix.platform.users.entity.Employee;
import com.civicfix.platform.users.enums.RoleType;

import java.time.LocalDate;

public class EmployeeDto {

    private Long id;
    private Long userId;
    private String username;
    private String employeeCode;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String designation;
    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private Long teamId;
    private String teamName;
    private String teamCode;
    private Long supervisorId;
    private String supervisorName;
    private String supervisorCode;
    private RoleType role;
    private LocalDate joiningDate;
    private boolean active;

    public EmployeeDto() {
    }

    public static EmployeeDto fromEntity(Employee e) {
        if (e == null) return null;
        EmployeeDto dto = new EmployeeDto();
        dto.setId(e.getId());
        if (e.getUser() != null) {
            dto.setUserId(e.getUser().getId());
            dto.setUsername(e.getUser().getUsername());
        }
        dto.setEmployeeCode(e.getEmployeeCode());
        dto.setFullName(e.getFullName());
        dto.setEmail(e.getEmail());
        dto.setPhoneNumber(e.getPhoneNumber());
        dto.setDesignation(e.getDesignation());
        if (e.getDepartment() != null) {
            dto.setDepartmentId(e.getDepartment().getId());
            dto.setDepartmentName(e.getDepartment().getName());
            dto.setDepartmentCode(e.getDepartment().getCode());
        }
        if (e.getTeam() != null) {
            dto.setTeamId(e.getTeam().getId());
            dto.setTeamName(e.getTeam().getName());
            dto.setTeamCode(e.getTeam().getCode());
        }
        if (e.getSupervisor() != null) {
            dto.setSupervisorId(e.getSupervisor().getId());
            dto.setSupervisorName(e.getSupervisor().getFullName());
            dto.setSupervisorCode(e.getSupervisor().getEmployeeCode());
        }
        dto.setRole(e.getRole());
        dto.setJoiningDate(e.getJoiningDate());
        dto.setActive(e.isActive());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
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

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getTeamCode() {
        return teamCode;
    }

    public void setTeamCode(String teamCode) {
        this.teamCode = teamCode;
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

    public RoleType getRole() {
        return role;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(LocalDate joiningDate) {
        this.joiningDate = joiningDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
