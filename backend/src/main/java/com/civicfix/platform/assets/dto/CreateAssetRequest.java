package com.civicfix.platform.assets.dto;

import com.civicfix.platform.assets.enums.AssetStatus;
import com.civicfix.platform.assets.enums.AssetType;
import com.civicfix.platform.assets.enums.ConditionRating;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateAssetRequest {

    @NotBlank(message = "Asset name is required")
    @Size(max = 150, message = "Asset name must not exceed 150 characters")
    private String name;

    @NotNull(message = "Asset type is required")
    private AssetType assetType;

    @NotNull(message = "Organization ID is required")
    private Long organizationId;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    private Long wardId;

    private String description;

    private AssetStatus status = AssetStatus.OPERATIONAL;

    private ConditionRating conditionRating = ConditionRating.GOOD;

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

    public CreateAssetRequest() {
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

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Long getWardId() {
        return wardId;
    }

    public void setWardId(Long wardId) {
        this.wardId = wardId;
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
}
