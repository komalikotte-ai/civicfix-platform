package com.civicfix.platform.assets.entity;

import com.civicfix.platform.assets.enums.AssetStatus;
import com.civicfix.platform.assets.enums.AssetType;
import com.civicfix.platform.assets.enums.ConditionRating;
import com.civicfix.platform.common.entity.BaseAuditableEntity;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.organizations.entity.Organization;
import com.civicfix.platform.users.entity.Employee;
import com.civicfix.platform.wards.entity.Ward;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assets", indexes = {
        @Index(name = "idx_asset_code", columnList = "asset_code"),
        @Index(name = "idx_asset_type", columnList = "asset_type"),
        @Index(name = "idx_asset_dept", columnList = "department_id"),
        @Index(name = "idx_asset_ward", columnList = "ward_id"),
        @Index(name = "idx_asset_status", columnList = "status"),
        @Index(name = "idx_asset_condition", columnList = "condition_rating")
})
public class Asset extends BaseAuditableEntity {

    @Column(name = "asset_code", nullable = false, unique = true, length = 50)
    private String assetCode;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false, length = 50)
    private AssetType assetType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id")
    private Ward ward;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AssetStatus status = AssetStatus.OPERATIONAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_rating", nullable = false, length = 30)
    private ConditionRating conditionRating = ConditionRating.GOOD;

    @Column(name = "address_line", columnDefinition = "TEXT")
    private String addressLine;

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "gis_geometry", columnDefinition = "TEXT")
    private String gisGeometry;

    @Column(name = "installation_date")
    private LocalDate installationDate;

    @Column(name = "commissioned_date")
    private LocalDate commissionedDate;

    @Column(name = "warranty_expiry_date")
    private LocalDate warrantyExpiryDate;

    @Column(name = "expected_lifecycle_years")
    private Integer expectedLifecycleYears;

    @Column(name = "estimated_replacement_cost", precision = 14, scale = 2)
    private BigDecimal estimatedReplacementCost = BigDecimal.ZERO;

    @Column(name = "manufacturer", length = 100)
    private String manufacturer;

    @Column(name = "model_number", length = 100)
    private String modelNumber;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(name = "specifications_json", columnDefinition = "TEXT")
    private String specificationsJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custodian_employee_id")
    private Employee custodian;

    @Column(name = "last_inspected_at")
    private Instant lastInspectedAt;

    @Column(name = "last_repaired_at")
    private Instant lastRepairedAt;

    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssetInspection> inspections = new ArrayList<>();

    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssetRepair> repairs = new ArrayList<>();

    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssetReplacement> replacements = new ArrayList<>();

    public Asset() {
    }

    public String getAssetCode() {
        return assetCode;
    }

    public void setAssetCode(String assetCode) {
        this.assetCode = assetCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public void setAssetType(AssetType assetType) {
        this.assetType = assetType;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Ward getWard() {
        return ward;
    }

    public void setWard(Ward ward) {
        this.ward = ward;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AssetStatus getStatus() {
        return status;
    }

    public void setStatus(AssetStatus status) {
        this.status = status;
    }

    public ConditionRating getConditionRating() {
        return conditionRating;
    }

    public void setConditionRating(ConditionRating conditionRating) {
        this.conditionRating = conditionRating;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getGisGeometry() {
        return gisGeometry;
    }

    public void setGisGeometry(String gisGeometry) {
        this.gisGeometry = gisGeometry;
    }

    public LocalDate getInstallationDate() {
        return installationDate;
    }

    public void setInstallationDate(LocalDate installationDate) {
        this.installationDate = installationDate;
    }

    public LocalDate getCommissionedDate() {
        return commissionedDate;
    }

    public void setCommissionedDate(LocalDate commissionedDate) {
        this.commissionedDate = commissionedDate;
    }

    public LocalDate getWarrantyExpiryDate() {
        return warrantyExpiryDate;
    }

    public void setWarrantyExpiryDate(LocalDate warrantyExpiryDate) {
        this.warrantyExpiryDate = warrantyExpiryDate;
    }

    public Integer getExpectedLifecycleYears() {
        return expectedLifecycleYears;
    }

    public void setExpectedLifecycleYears(Integer expectedLifecycleYears) {
        this.expectedLifecycleYears = expectedLifecycleYears;
    }

    public BigDecimal getEstimatedReplacementCost() {
        return estimatedReplacementCost;
    }

    public void setEstimatedReplacementCost(BigDecimal estimatedReplacementCost) {
        this.estimatedReplacementCost = estimatedReplacementCost;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getSpecificationsJson() {
        return specificationsJson;
    }

    public void setSpecificationsJson(String specificationsJson) {
        this.specificationsJson = specificationsJson;
    }

    public Employee getCustodian() {
        return custodian;
    }

    public void setCustodian(Employee custodian) {
        this.custodian = custodian;
    }

    public Instant getLastInspectedAt() {
        return lastInspectedAt;
    }

    public void setLastInspectedAt(Instant lastInspectedAt) {
        this.lastInspectedAt = lastInspectedAt;
    }

    public Instant getLastRepairedAt() {
        return lastRepairedAt;
    }

    public void setLastRepairedAt(Instant lastRepairedAt) {
        this.lastRepairedAt = lastRepairedAt;
    }

    public List<AssetInspection> getInspections() {
        return inspections;
    }

    public void setInspections(List<AssetInspection> inspections) {
        this.inspections = inspections;
    }

    public List<AssetRepair> getRepairs() {
        return repairs;
    }

    public void setRepairs(List<AssetRepair> repairs) {
        this.repairs = repairs;
    }

    public List<AssetReplacement> getReplacements() {
        return replacements;
    }

    public void setReplacements(List<AssetReplacement> replacements) {
        this.replacements = replacements;
    }
}
