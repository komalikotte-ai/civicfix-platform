package com.civicfix.platform.departments.dto;

import jakarta.validation.constraints.Size;

public class UpdateTeamRequest {

    @Size(max = 100, message = "Team name cannot exceed 100 characters")
    private String name;

    private String description;

    private Long supervisorId;

    private Boolean active;

    public UpdateTeamRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
