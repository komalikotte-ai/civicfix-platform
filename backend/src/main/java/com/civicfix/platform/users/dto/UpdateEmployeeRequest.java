package com.civicfix.platform.users.dto;

import com.civicfix.platform.users.enums.RoleType;
import jakarta.validation.constraints.Size;

public class UpdateEmployeeRequest {

    @Size(max = 150, message = "Full name cannot exceed 150 characters")
    private String fullName;

    @Size(max = 25, message = "Phone number cannot exceed 25 characters")
    private String phoneNumber;

    @Size(max = 100, message = "Designation cannot exceed 100 characters")
    private String designation;

    private RoleType role;

    private Boolean active;

    public UpdateEmployeeRequest() {
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public RoleType getRole() {
        return role;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
