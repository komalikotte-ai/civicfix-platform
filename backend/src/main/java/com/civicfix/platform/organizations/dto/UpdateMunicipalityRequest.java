package com.civicfix.platform.organizations.dto;

import com.civicfix.platform.organizations.enums.MunicipalityType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UpdateMunicipalityRequest {

    @Size(max = 150, message = "Municipality name must not exceed 150 characters")
    private String name;

    private MunicipalityType type;

    @Email(message = "Contact email must be valid")
    @Size(max = 150, message = "Contact email must not exceed 150 characters")
    private String contactEmail;

    @Size(max = 25, message = "Contact phone must not exceed 25 characters")
    private String contactPhone;

    private String address;

    @Size(max = 255, message = "Website URL must not exceed 255 characters")
    private String website;

    private Boolean active;

    public UpdateMunicipalityRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MunicipalityType getType() {
        return type;
    }

    public void setType(MunicipalityType type) {
        this.type = type;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
