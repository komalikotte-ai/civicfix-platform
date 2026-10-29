package com.civicfix.platform.organizations.dto;

import com.civicfix.platform.organizations.entity.City;

public class CityDto {

    private Long id;
    private String name;
    private String code;
    private String state;
    private String country;
    private String timezone;
    private boolean active;

    public CityDto() {
    }

    public static CityDto fromEntity(City city) {
        if (city == null) return null;
        CityDto dto = new CityDto();
        dto.setId(city.getId());
        dto.setName(city.getName());
        dto.setCode(city.getCode());
        dto.setState(city.getState());
        dto.setCountry(city.getCountry());
        dto.setTimezone(city.getTimezone());
        dto.setActive(city.isActive());
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
