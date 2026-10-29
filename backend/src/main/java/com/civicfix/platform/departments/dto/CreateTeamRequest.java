package com.civicfix.platform.departments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateTeamRequest {

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotBlank(message = "Team name is required")
    @Size(max = 100, message = "Team name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "Team code is required")
    @Size(max = 50, message = "Team code cannot exceed 50 characters")
    private String code;

    private String description;

    private Long supervisorId;

    public CreateTeamRequest() {
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
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
}
