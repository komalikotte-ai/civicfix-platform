package com.civicfix.platform.assets.dto;

import com.civicfix.platform.assets.enums.AssetStatus;
import com.civicfix.platform.assets.enums.ConditionRating;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RecordInspectionRequest {

    private Long workOrderId;

    private Long inspectorId;

    @NotNull(message = "Condition rating is required")
    private ConditionRating conditionRating;

    @NotNull(message = "Operational status is required")
    private AssetStatus operationalStatus;

    @NotBlank(message = "Findings cannot be empty")
    private String findings;

    private String recommendations;

    private String defectsObserved;

    @Min(value = 0, message = "Score must be at least 0")
    @Max(value = 100, message = "Score cannot exceed 100")
    private Integer score;

    private String photosJson;

    public RecordInspectionRequest() {
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    public Long getInspectorId() {
        return inspectorId;
    }

    public void setInspectorId(Long inspectorId) {
        this.inspectorId = inspectorId;
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
}
