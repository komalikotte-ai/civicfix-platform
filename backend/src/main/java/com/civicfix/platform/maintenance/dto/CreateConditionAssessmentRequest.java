package com.civicfix.platform.maintenance.dto;

import com.civicfix.platform.assets.enums.ConditionRating;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CreateConditionAssessmentRequest {

    private String assessmentCode;

    @NotNull(message = "Asset ID is required")
    private Long assetId;

    private Long inspectorId;

    @NotNull(message = "Overall condition is required")
    private ConditionRating overallCondition;

    @NotNull(message = "Health index score is required")
    @Min(value = 0, message = "Health index score cannot be negative")
    @Max(value = 100, message = "Health index score cannot exceed 100")
    private Integer healthIndexScore;

    private String structuralHealth;
    private String functionalPerformance;
    private String safetyRiskAssessment;
    private BigDecimal estimatedRemainingUsefulLifeYears;

    @NotBlank(message = "Assessment notes are required")
    private String assessmentNotes;

    private String photosJson;

    public CreateConditionAssessmentRequest() {}

    public String getAssessmentCode() { return assessmentCode; }
    public void setAssessmentCode(String assessmentCode) { this.assessmentCode = assessmentCode; }
    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }
    public Long getInspectorId() { return inspectorId; }
    public void setInspectorId(Long inspectorId) { this.inspectorId = inspectorId; }
    public ConditionRating getOverallCondition() { return overallCondition; }
    public void setOverallCondition(ConditionRating overallCondition) { this.overallCondition = overallCondition; }
    public Integer getHealthIndexScore() { return healthIndexScore; }
    public void setHealthIndexScore(Integer healthIndexScore) { this.healthIndexScore = healthIndexScore; }
    public String getStructuralHealth() { return structuralHealth; }
    public void setStructuralHealth(String structuralHealth) { this.structuralHealth = structuralHealth; }
    public String getFunctionalPerformance() { return functionalPerformance; }
    public void setFunctionalPerformance(String functionalPerformance) { this.functionalPerformance = functionalPerformance; }
    public String getSafetyRiskAssessment() { return safetyRiskAssessment; }
    public void setSafetyRiskAssessment(String safetyRiskAssessment) { this.safetyRiskAssessment = safetyRiskAssessment; }
    public BigDecimal getEstimatedRemainingUsefulLifeYears() { return estimatedRemainingUsefulLifeYears; }
    public void setEstimatedRemainingUsefulLifeYears(BigDecimal estimatedRemainingUsefulLifeYears) { this.estimatedRemainingUsefulLifeYears = estimatedRemainingUsefulLifeYears; }
    public String getAssessmentNotes() { return assessmentNotes; }
    public void setAssessmentNotes(String assessmentNotes) { this.assessmentNotes = assessmentNotes; }
    public String getPhotosJson() { return photosJson; }
    public void setPhotosJson(String photosJson) { this.photosJson = photosJson; }
}
