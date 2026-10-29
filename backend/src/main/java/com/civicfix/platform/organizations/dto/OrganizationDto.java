package com.civicfix.platform.organizations.dto;

import com.civicfix.platform.organizations.entity.Organization;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class OrganizationDto {

    private Long id;

    @NotBlank(message = "Organization name is required")
    @Size(max = 150, message = "Organization name must not exceed 150 characters")
    private String name;

    @NotBlank(message = "Organization code is required")
    @Size(max = 50, message = "Organization code must not exceed 50 characters")
    private String code;

    @NotBlank(message = "State is required")
    private String state;

    private String country;
    private String contactEmail;
    private String contactPhone;
    private String timezone;
    private boolean active;

    public OrganizationDto() {
    }

    public static OrganizationDto fromEntity(Organization entity) {
        OrganizationDto dto = new OrganizationDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCode(entity.getCode());
        dto.setState(entity.getState());
        dto.setCountry(entity.getCountry());
        dto.setContactEmail(entity.getContactEmail());
        dto.setContactPhone(entity.getContactPhone());
        dto.setTimezone(entity.getTimezone());
        dto.setActive(entity.isActive());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
