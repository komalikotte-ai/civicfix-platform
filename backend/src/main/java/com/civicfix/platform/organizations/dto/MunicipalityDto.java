package com.civicfix.platform.organizations.dto;

import com.civicfix.platform.organizations.entity.Municipality;
import com.civicfix.platform.organizations.enums.MunicipalityType;

public class MunicipalityDto {

    private Long id;
    private Long cityId;
    private String cityName;
    private String cityCode;
    private String name;
    private String code;
    private MunicipalityType type;
    private String contactEmail;
    private String contactPhone;
    private String address;
    private String website;
    private boolean active;

    public MunicipalityDto() {
    }

    public static MunicipalityDto fromEntity(Municipality m) {
        if (m == null) return null;
        MunicipalityDto dto = new MunicipalityDto();
        dto.setId(m.getId());
        if (m.getCity() != null) {
            dto.setCityId(m.getCity().getId());
            dto.setCityName(m.getCity().getName());
            dto.setCityCode(m.getCity().getCode());
        }
        dto.setName(m.getName());
        dto.setCode(m.getCode());
        dto.setType(m.getType());
        dto.setContactEmail(m.getContactEmail());
        dto.setContactPhone(m.getContactPhone());
        dto.setAddress(m.getAddress());
        dto.setWebsite(m.getWebsite());
        dto.setActive(m.isActive());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCityId() {
        return cityId;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
