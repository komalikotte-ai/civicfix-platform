package com.civicfix.platform.departments.dto;

import com.civicfix.platform.departments.enums.DepartmentStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateDepartmentRequest {

    private Long municipalityId;
    private Long organizationId;

    @NotBlank(message = "Department name is required")
    @Size(max = 100, message = "Department name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "Department code is required")
    @Size(max = 50, message = "Department code cannot exceed 50 characters")
    private String code;

    private String description;

    @Email(message = "Escalation email must be valid")
    @Size(max = 150, message = "Escalation email cannot exceed 150 characters")
    private String escalationEmail;

    @Size(max = 25, message = "Contact phone cannot exceed 25 characters")
    private String contactPhone;

    private DepartmentStatus status = DepartmentStatus.ACTIVE;

    public CreateDepartmentRequest() {
    }

    public Long getMunicipalityId() {
        return municipalityId;
    }

    public void setMunicipalityId(Long municipalityId) {
        this.municipalityId = municipalityId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
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

    public String getEscalationEmail() {
        return escalationEmail;
    }

    public void setEscalationEmail(String escalationEmail) {
        this.escalationEmail = escalationEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public DepartmentStatus getStatus() {
        return status;
    }

    public void setStatus(DepartmentStatus status) {
        this.status = status;
    }
}
