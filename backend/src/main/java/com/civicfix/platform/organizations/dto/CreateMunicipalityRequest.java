package com.civicfix.platform.organizations.dto;

import com.civicfix.platform.organizations.enums.MunicipalityType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateMunicipalityRequest {

    @NotNull(message = "City ID is required")
    private Long cityId;

    @NotBlank(message = "Municipality name is required")
    @Size(max = 150, message = "Municipality name must not exceed 150 characters")
    private String name;

    @NotBlank(message = "Municipality code is required")
    @Size(max = 50, message = "Municipality code must not exceed 50 characters")
    private String code;

    private MunicipalityType type = MunicipalityType.MUNICIPAL_CORPORATION;

    @Email(message = "Contact email must be valid")
    @Size(max = 150, message = "Contact email must not exceed 150 characters")
    private String contactEmail;

    @Size(max = 25, message = "Contact phone must not exceed 25 characters")
    private String contactPhone;

    private String address;

    @Size(max = 255, message = "Website URL must not exceed 255 characters")
    private String website;

    public CreateMunicipalityRequest() {
    }

    public Long getCityId() {
        return cityId;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
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
}
