package com.civicfix.platform.maintenance.entity;

import com.civicfix.platform.assets.entity.Asset;
import com.civicfix.platform.assets.enums.ConditionRating;
import com.civicfix.platform.common.entity.BaseAuditableEntity;
import com.civicfix.platform.users.entity.Employee;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "condition_assessments", indexes = {
        @Index(name = "idx_cond_code", columnList = "assessment_code"),
        @Index(name = "idx_cond_asset", columnList = "asset_id"),
        @Index(name = "idx_cond_date", columnList = "assessment_date"),
        @Index(name = "idx_cond_rating", columnList = "overall_condition")
})
public class ConditionAssessment extends BaseAuditableEntity {

    @Column(name = "assessment_code", nullable = false, unique = true, length = 50)
    private String assessmentCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspector_id")
    private Employee inspector;

    @Column(name = "assessment_date", nullable = false)
    private Instant assessmentDate = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_condition", nullable = false, length = 30)
    private ConditionRating overallCondition = ConditionRating.GOOD;

    @Column(name = "health_index_score", nullable = false)
    private int healthIndexScore;

    @Column(name = "previous_health_index_score")
    private Integer previousHealthIndexScore;

    @Column(name = "degradation_rate", precision = 6, scale = 2)
    private BigDecimal degradationRate = BigDecimal.ZERO;

    @Column(name = "structural_health", length = 100)
    private String structuralHealth;

    @Column(name = "functional_performance", length = 100)
    private String functionalPerformance;

    @Column(name = "safety_risk_assessment", length = 100)
    private String safetyRiskAssessment;

    @Column(name = "estimated_remaining_useful_life_years", precision = 5, scale = 1)
    private BigDecimal estimatedRemainingUsefulLifeYears;

    @Column(name = "assessment_notes", nullable = false, columnDefinition = "TEXT")
    private String assessmentNotes;

    @Column(name = "photos_json", columnDefinition = "TEXT")
    private String photosJson;

    public ConditionAssessment() {}

    public String getAssessmentCode() { return assessmentCode; }
    public void setAssessmentCode(String assessmentCode) { this.assessmentCode = assessmentCode; }
    public Asset getAsset() { return asset; }
    public void setAsset(Asset asset) { this.asset = asset; }
    public Employee getInspector() { return inspector; }
    public void setInspector(Employee inspector) { this.inspector = inspector; }
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
}
