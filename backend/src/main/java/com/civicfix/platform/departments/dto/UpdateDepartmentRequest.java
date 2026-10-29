package com.civicfix.platform.departments.dto;

import com.civicfix.platform.departments.enums.DepartmentStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UpdateDepartmentRequest {

    @Size(max = 100, message = "Department name cannot exceed 100 characters")
    private String name;

    private String description;

    @Email(message = "Escalation email must be valid")
    @Size(max = 150, message = "Escalation email cannot exceed 150 characters")
    private String escalationEmail;

    @Size(max = 25, message = "Contact phone cannot exceed 25 characters")
    private String contactPhone;

    private DepartmentStatus status;

    private Boolean active;

    public UpdateDepartmentRequest() {
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
