package com.civicfix.platform.organizations.entity;

import com.civicfix.platform.common.entity.BaseAuditableEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "organizations")
public class Organization extends BaseAuditableEntity {

    @Column(name = "name", nullable = false, unique = true, length = 150)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "country", nullable = false, length = 100)
    private String country = "India";

    @Column(name = "contact_email", length = 150)
    private String contactEmail;

    @Column(name = "contact_phone", length = 25)
    private String contactPhone;

    @Column(name = "timezone", nullable = false, length = 50)
    private String timezone = "Asia/Kolkata";

    public Organization() {
    }

    public Organization(String name, String code, String state) {
        this.name = name;
        this.code = code;
        this.state = state;
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
}
