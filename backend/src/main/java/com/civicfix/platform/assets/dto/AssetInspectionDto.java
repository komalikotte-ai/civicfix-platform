package com.civicfix.platform.assets.dto;

import com.civicfix.platform.assets.entity.AssetInspection;
import com.civicfix.platform.assets.enums.AssetStatus;
import com.civicfix.platform.assets.enums.ConditionRating;

import java.time.Instant;

public class AssetInspectionDto {

    private Long id;
    private Long assetId;
    private String assetCode;
    private String assetName;
    private Long workOrderId;
    private String workOrderNumber;
    private Long inspectorId;
    private String inspectorName;
    private Instant inspectionDate;
    private ConditionRating conditionRating;
    private AssetStatus operationalStatus;
    private String findings;
    private String recommendations;
    private String defectsObserved;
    private Integer score;
    private String photosJson;
    private Instant createdAt;
    private String createdBy;

    public AssetInspectionDto() {
    }

    public static AssetInspectionDto fromEntity(AssetInspection entity) {
        if (entity == null) return null;

        AssetInspectionDto dto = new AssetInspectionDto();
        dto.setId(entity.getId());
        if (entity.getAsset() != null) {
            dto.setAssetId(entity.getAsset().getId());
            dto.setAssetCode(entity.getAsset().getAssetCode());
            dto.setAssetName(entity.getAsset().getName());
        }
        if (entity.getWorkOrder() != null) {
            dto.setWorkOrderId(entity.getWorkOrder().getId());
            dto.setWorkOrderNumber(entity.getWorkOrder().getWorkOrderNumber());
        }
        if (entity.getInspector() != null) {
            dto.setInspectorId(entity.getInspector().getId());
            dto.setInspectorName(entity.getInspector().getFullName());
        }
        dto.setInspectionDate(entity.getInspectionDate());
        dto.setConditionRating(entity.getConditionRating());
        dto.setOperationalStatus(entity.getOperationalStatus());
        dto.setFindings(entity.getFindings());
        dto.setRecommendations(entity.getRecommendations());
        dto.setDefectsObserved(entity.getDefectsObserved());
        dto.setScore(entity.getScore());
        dto.setPhotosJson(entity.getPhotosJson());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setCreatedBy(entity.getCreatedBy());

        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public String getAssetCode() {
        return assetCode;
    }

    public void setAssetCode(String assetCode) {
        this.assetCode = assetCode;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    public String getWorkOrderNumber() {
        return workOrderNumber;
    }

    public void setWorkOrderNumber(String workOrderNumber) {
        this.workOrderNumber = workOrderNumber;
    }

    public Long getInspectorId() {
        return inspectorId;
    }

    public void setInspectorId(Long inspectorId) {
        this.inspectorId = inspectorId;
    }

    public String getInspectorName() {
        return inspectorName;
    }

    public void setInspectorName(String inspectorName) {
        this.inspectorName = inspectorName;
    }

    public Instant getInspectionDate() {
        return inspectionDate;
    }

    public void setInspectionDate(Instant inspectionDate) {
        this.inspectionDate = inspectionDate;
    }

    public ConditionRating getConditionRating() {
        return conditionRating;
    }

    public void setConditionRating(ConditionRating conditionRating) {
        this.conditionRating = conditionRating;
    }

    public AssetStatus getOperationalStatus() {
        return operationalStatus;
    }

    public void setOperationalStatus(AssetStatus operationalStatus) {
        this.operationalStatus = operationalStatus;
    }

    public String getFindings() {
        return findings;
    }

    public void setFindings(String findings) {
        this.findings = findings;
    }

    public String getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }

    public String getDefectsObserved() {
        return defectsObserved;
    }

    public void setDefectsObserved(String defectsObserved) {
        this.defectsObserved = defectsObserved;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getPhotosJson() {
        return photosJson;
    }

    public void setPhotosJson(String photosJson) {
        this.photosJson = photosJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
