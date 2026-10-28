package com.civicfix.platform.wards.entity;

import com.civicfix.platform.common.entity.BaseAuditableEntity;
import com.civicfix.platform.organizations.entity.Municipality;
import com.civicfix.platform.organizations.entity.Organization;
import jakarta.persistence.*;

@Entity
@Table(name = "wards")
public class Ward extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipality_id")
    private Municipality municipality;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(name = "ward_number", nullable = false, length = 20)
    private String wardNumber;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "zone_name", length = 100)
    private String zoneName;

    @Column(name = "boundary_geojson", columnDefinition = "TEXT")
    private String boundaryGeoJson;

    @Column(name = "center_latitude")
    private Double centerLatitude;

    @Column(name = "center_longitude")
    private Double centerLongitude;

    @Column(name = "area_sq_km")
    private Double areaSqKm;

    @Column(name = "population")
    private Integer population;

    public Ward() {
    }

    public Ward(Municipality municipality, String wardNumber, String name) {
        this.municipality = municipality;
        this.wardNumber = wardNumber;
        this.name = name;
    }

    public Ward(Organization organization, String wardNumber, String name) {
        this.organization = organization;
        this.wardNumber = wardNumber;
        this.name = name;
    }

    public Municipality getMunicipality() {
        return municipality;
    }

    public void setMunicipality(Municipality municipality) {
        this.municipality = municipality;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
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
