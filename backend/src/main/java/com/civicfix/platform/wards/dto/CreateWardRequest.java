package com.civicfix.platform.wards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateWardRequest {

    private Long municipalityId;
    private Long organizationId;

    @NotBlank(message = "Ward number is required")
    @Size(max = 20, message = "Ward number cannot exceed 20 characters")
    private String wardNumber;

    @NotBlank(message = "Ward name is required")
    @Size(max = 100, message = "Ward name cannot exceed 100 characters")
    private String name;

    @Size(max = 100, message = "Zone name cannot exceed 100 characters")
    private String zoneName;

    private String boundaryGeoJson;
    private Double centerLatitude;
    private Double centerLongitude;
    private Double areaSqKm;
    private Integer population;

    public CreateWardRequest() {
    }

    public Long getMunicipalityId() {
        return municipalityId;
    }

    public void setMunicipalityId(Long municipalityId) {
        this.municipalityId = municipalityId;
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
}
