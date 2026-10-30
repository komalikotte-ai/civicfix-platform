package com.civicfix.platform.assets.dto;

import com.civicfix.platform.assets.entity.Asset;
import com.civicfix.platform.assets.enums.AssetStatus;
import com.civicfix.platform.assets.enums.AssetType;
import com.civicfix.platform.assets.enums.ConditionRating;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class AssetDto {

    private Long id;
    private String assetCode;
    private String name;
    private AssetType assetType;
    private Long organizationId;
    private String organizationName;
    private Long departmentId;
    private String departmentName;
    private Long wardId;
    private String wardName;
    private String description;
    private AssetStatus status;
    private ConditionRating conditionRating;
    private String addressLine;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String gisGeometry;
    private LocalDate installationDate;
    private LocalDate commissionedDate;
    private LocalDate warrantyExpiryDate;
    private Integer expectedLifecycleYears;
    private BigDecimal estimatedReplacementCost;
    private String manufacturer;
    private String modelNumber;
    private String serialNumber;
    private String specificationsJson;
    private Long custodianEmployeeId;
    private String custodianName;
    private Instant lastInspectedAt;
    private Instant lastRepairedAt;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public AssetDto() {
    }

    public static AssetDto fromEntity(Asset entity) {
        if (entity == null) return null;

        AssetDto dto = new AssetDto();
        dto.setId(entity.getId());
        dto.setAssetCode(entity.getAssetCode());
        dto.setName(entity.getName());
        dto.setAssetType(entity.getAssetType());

        if (entity.getOrganization() != null) {
            dto.setOrganizationId(entity.getOrganization().getId());
            dto.setOrganizationName(entity.getOrganization().getName());
        }

        if (entity.getDepartment() != null) {
            dto.setDepartmentId(entity.getDepartment().getId());
            dto.setDepartmentName(entity.getDepartment().getName());
        }

        if (entity.getWard() != null) {
            dto.setWardId(entity.getWard().getId());
            dto.setWardName(entity.getWard().getName());
        }

        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        dto.setConditionRating(entity.getConditionRating());
        dto.setAddressLine(entity.getAddressLine());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setGisGeometry(entity.getGisGeometry());
        dto.setInstallationDate(entity.getInstallationDate());
        dto.setCommissionedDate(entity.getCommissionedDate());
        dto.setWarrantyExpiryDate(entity.getWarrantyExpiryDate());
        dto.setExpectedLifecycleYears(entity.getExpectedLifecycleYears());
        dto.setEstimatedReplacementCost(entity.getEstimatedReplacementCost());
        dto.setManufacturer(entity.getManufacturer());
        dto.setModelNumber(entity.getModelNumber());
        dto.setSerialNumber(entity.getSerialNumber());
        dto.setSpecificationsJson(entity.getSpecificationsJson());

        if (entity.getCustodian() != null) {
            dto.setCustodianEmployeeId(entity.getCustodian().getId());
            dto.setCustodianName(entity.getCustodian().getFullName());
        }

        dto.setLastInspectedAt(entity.getLastInspectedAt());
        dto.setLastRepairedAt(entity.getLastRepairedAt());
        dto.setActive(entity.isActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Long getWardId() {
        return wardId;
    }

    public void setWardId(Long wardId) {
        this.wardId = wardId;
    }

    public String getWardName() {
        return wardName;
    }

    public void setWardName(String wardName) {
        this.wardName = wardName;
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

    public Long getCustodianEmployeeId() {
        return custodianEmployeeId;
    }

    public void setCustodianEmployeeId(Long custodianEmployeeId) {
        this.custodianEmployeeId = custodianEmployeeId;
    }

    public String getCustodianName() {
        return custodianName;
    }

    public void setCustodianName(String custodianName) {
        this.custodianName = custodianName;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
