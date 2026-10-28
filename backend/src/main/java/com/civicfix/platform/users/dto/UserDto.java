package com.civicfix.platform.users.dto;

import com.civicfix.platform.users.entity.Role;
import com.civicfix.platform.users.entity.User;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

public class UserDto {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private Long organizationId;
    private String organizationName;
    private Long departmentId;
    private String departmentName;
    private Long wardId;
    private String wardNumber;
    private Set<String> roles;
    private boolean active;
    private Instant createdAt;

    public UserDto() {
    }

    public static UserDto fromEntity(User entity) {
        UserDto dto = new UserDto();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setEmail(entity.getEmail());
        dto.setFullName(entity.getFullName());
        dto.setPhoneNumber(entity.getPhoneNumber());
        if (entity.getOrganization() != null) {
            dto.setOrganizationId(entity.getOrganization().getId());
            dto.setOrganizationName(entity.getOrganization().getName());
        }
        if (entity.getDepartment() != null) {
            dto.setDepartmentId(entity.getDepartment().getId());
            dto.setDepartmentName(entity.getDepartment().getName());
        }
        if (entity.getWard() != null) {
            dto.setWardId(entity.getWard().getId());
            dto.setWardNumber(entity.getWard().getWardNumber());
        }
        dto.setRoles(entity.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet()));
        dto.setActive(entity.isActive());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
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

    public Long getWardId() {
        return wardId;
    }

    public void setWardId(Long wardId) {
        this.wardId = wardId;
    }

    public String getWardNumber() {
        return wardNumber;
    }

    public void setWardNumber(String wardNumber) {
        this.wardNumber = wardNumber;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
