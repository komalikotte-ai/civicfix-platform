package com.civicfix.platform.wards.dto;

import com.civicfix.platform.wards.entity.Ward;

public class WardDto {

    private Long id;
    private Long municipalityId;
    private String municipalityName;
    private Long organizationId;
    private String wardNumber;
    private String name;
    private String zoneName;
    private String boundaryGeoJson;
    private Double centerLatitude;
    private Double centerLongitude;
    private Double areaSqKm;
    private Integer population;
    private boolean active;

    public WardDto() {
    }

    public static WardDto fromEntity(Ward entity) {
        if (entity == null) return null;
        WardDto dto = new WardDto();
        dto.setId(entity.getId());
        if (entity.getMunicipality() != null) {
            dto.setMunicipalityId(entity.getMunicipality().getId());
            dto.setMunicipalityName(entity.getMunicipality().getName());
        }
        if (entity.getOrganization() != null) {
            dto.setOrganizationId(entity.getOrganization().getId());
        }
        dto.setWardNumber(entity.getWardNumber());
        dto.setName(entity.getName());
        dto.setZoneName(entity.getZoneName());
        dto.setBoundaryGeoJson(entity.getBoundaryGeoJson());
        dto.setCenterLatitude(entity.getCenterLatitude());
        dto.setCenterLongitude(entity.getCenterLongitude());
        dto.setAreaSqKm(entity.getAreaSqKm());
        dto.setPopulation(entity.getPopulation());
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

    public String getWardNumber() {
        return wardNumber;
    }

    public void setWardNumber(String wardNumber) {
        this.wardNumber = wardNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public String getBoundaryGeoJson() {
        return boundaryGeoJson;
    }

    public void setBoundaryGeoJson(String boundaryGeoJson) {
        this.boundaryGeoJson = boundaryGeoJson;
    }

    public Double getCenterLatitude() {
        return centerLatitude;
    }

    public void setCenterLatitude(Double centerLatitude) {
        this.centerLatitude = centerLatitude;
    }

    public Double getCenterLongitude() {
        return centerLongitude;
    }

    public void setCenterLongitude(Double centerLongitude) {
        this.centerLongitude = centerLongitude;
    }

    public Double getAreaSqKm() {
        return areaSqKm;
    }

    public void setAreaSqKm(Double areaSqKm) {
        this.areaSqKm = areaSqKm;
    }

    public Integer getPopulation() {
        return population;
    }

    public void setPopulation(Integer population) {
        this.population = population;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
