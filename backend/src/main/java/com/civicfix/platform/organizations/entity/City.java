package com.civicfix.platform.organizations.entity;

import com.civicfix.platform.common.entity.BaseAuditableEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "cities")
public class City extends BaseAuditableEntity {

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "country", nullable = false, length = 100)
    private String country = "India";

    @Column(name = "timezone", nullable = false, length = 50)
    private String timezone = "Asia/Kolkata";

    public City() {
    }

    public City(String name, String code, String state) {
        this.name = name;
        this.code = code;
        this.state = state;
    }

    public City(String name, String code, String state, String country, String timezone) {
        this.name = name;
        this.code = code;
        this.state = state;
        if (country != null) this.country = country;
        if (timezone != null) this.timezone = timezone;
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

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}
