package com.civicfix.platform.organizations.entity;

import com.civicfix.platform.common.entity.BaseAuditableEntity;
import com.civicfix.platform.organizations.enums.MunicipalityType;
import jakarta.persistence.*;

@Entity
@Table(name = "municipalities")
public class Municipality extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(name = "name", nullable = false, unique = true, length = 150)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private MunicipalityType type = MunicipalityType.MUNICIPAL_CORPORATION;

    @Column(name = "contact_email", length = 150)
    private String contactEmail;

    @Column(name = "contact_phone", length = 25)
    private String contactPhone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "website", length = 255)
    private String website;

    public Municipality() {
    }

    public Municipality(City city, String name, String code, MunicipalityType type) {
        this.city = city;
        this.name = name;
        this.code = code;
        this.type = type;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
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
