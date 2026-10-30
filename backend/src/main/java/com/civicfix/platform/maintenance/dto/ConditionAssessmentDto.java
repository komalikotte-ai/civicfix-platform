package com.civicfix.platform.maintenance.dto;

import com.civicfix.platform.assets.enums.ConditionRating;
import com.civicfix.platform.maintenance.entity.ConditionAssessment;

import java.math.BigDecimal;
import java.time.Instant;

public class ConditionAssessmentDto {

    private Long id;
    private String assessmentCode;
    private Long assetId;
    private String assetCode;
    private String assetName;
    private Long inspectorId;
    private String inspectorName;
    private Instant assessmentDate;
    private ConditionRating overallCondition;
    private int healthIndexScore;
    private Integer previousHealthIndexScore;
    private BigDecimal degradationRate;
    private String structuralHealth;
    private String functionalPerformance;
    private String safetyRiskAssessment;
    private BigDecimal estimatedRemainingUsefulLifeYears;
    private String assessmentNotes;
    private String photosJson;
    private Instant createdAt;

    public ConditionAssessmentDto() {}

    public static ConditionAssessmentDto fromEntity(ConditionAssessment ca) {
        if (ca == null) return null;
        ConditionAssessmentDto dto = new ConditionAssessmentDto();
        dto.setId(ca.getId());
        dto.setAssessmentCode(ca.getAssessmentCode());
        if (ca.getAsset() != null) {
            dto.setAssetId(ca.getAsset().getId());
            dto.setAssetCode(ca.getAsset().getAssetCode());
            dto.setAssetName(ca.getAsset().getName());
        }
        if (ca.getInspector() != null) {
            dto.setInspectorId(ca.getInspector().getId());
            dto.setInspectorName(ca.getInspector().getFullName());
        }
        dto.setAssessmentDate(ca.getAssessmentDate());
        dto.setOverallCondition(ca.getOverallCondition());
        dto.setHealthIndexScore(ca.getHealthIndexScore());
        dto.setPreviousHealthIndexScore(ca.getPreviousHealthIndexScore());
        dto.setDegradationRate(ca.getDegradationRate());
        dto.setStructuralHealth(ca.getStructuralHealth());
        dto.setFunctionalPerformance(ca.getFunctionalPerformance());
        dto.setSafetyRiskAssessment(ca.getSafetyRiskAssessment());
        dto.setEstimatedRemainingUsefulLifeYears(ca.getEstimatedRemainingUsefulLifeYears());
        dto.setAssessmentNotes(ca.getAssessmentNotes());
        dto.setPhotosJson(ca.getPhotosJson());
        dto.setCreatedAt(ca.getCreatedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAssessmentCode() { return assessmentCode; }
    public void setAssessmentCode(String assessmentCode) { this.assessmentCode = assessmentCode; }
    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }
    public String getAssetCode() { return assetCode; }
    public void setAssetCode(String assetCode) { this.assetCode = assetCode; }
    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }
    public Long getInspectorId() { return inspectorId; }
    public void setInspectorId(Long inspectorId) { this.inspectorId = inspectorId; }
    public String getInspectorName() { return inspectorName; }
    public void setInspectorName(String inspectorName) { this.inspectorName = inspectorName; }
    public Instant getAssessmentDate() { return assessmentDate; }
    public void setAssessmentDate(Instant assessmentDate) { this.assessmentDate = assessmentDate; }
    public ConditionRating getOverallCondition() { return overallCondition; }
    public void setOverallCondition(ConditionRating overallCondition) { this.overallCondition = overallCondition; }
    public int getHealthIndexScore() { return healthIndexScore; }
    public void setHealthIndexScore(int healthIndexScore) { this.healthIndexScore = healthIndexScore; }
    public Integer getPreviousHealthIndexScore() { return previousHealthIndexScore; }
    public void setPreviousHealthIndexScore(Integer previousHealthIndexScore) { this.previousHealthIndexScore = previousHealthIndexScore; }
    public BigDecimal getDegradationRate() { return degradationRate; }
    public void setDegradationRate(BigDecimal degradationRate) { this.degradationRate = degradationRate; }
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
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
