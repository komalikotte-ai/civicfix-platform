package com.civicfix.platform.departments.dto;

import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.departments.enums.DepartmentStatus;

public class DepartmentDto {

    private Long id;
    private Long municipalityId;
    private String municipalityName;
    private Long organizationId;
    private String name;
    private String code;
    private String description;
    private String escalationEmail;
    private String contactPhone;
    private DepartmentStatus status;
    private boolean active;

    public DepartmentDto() {
    }

    public static DepartmentDto fromEntity(Department entity) {
        if (entity == null) return null;
        DepartmentDto dto = new DepartmentDto();
        dto.setId(entity.getId());
        if (entity.getMunicipality() != null) {
            dto.setMunicipalityId(entity.getMunicipality().getId());
            dto.setMunicipalityName(entity.getMunicipality().getName());
        }
        if (entity.getOrganization() != null) {
            dto.setOrganizationId(entity.getOrganization().getId());
        }
        dto.setName(entity.getName());
        dto.setCode(entity.getCode());
        dto.setDescription(entity.getDescription());
        dto.setEscalationEmail(entity.getEscalationEmail());
        dto.setContactPhone(entity.getContactPhone());
        dto.setStatus(entity.getStatus());
        dto.setActive(entity.isActive());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMunicipalityId() {
        return municipalityId;
    }

    public void setMunicipalityId(Long municipalityId) {
        this.municipalityId = municipalityId;
    }

    public String getMunicipalityName() {
        return municipalityName;
    }

    public void setMunicipalityName(String municipalityName) {
        this.municipalityName = municipalityName;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
