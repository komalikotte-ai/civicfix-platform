package com.civicfix.platform.maintenance.dto;

import com.civicfix.platform.maintenance.entity.RepairRecommendation;
import com.civicfix.platform.maintenance.enums.RecommendationAction;
import com.civicfix.platform.maintenance.enums.RecommendationStatus;
import com.civicfix.platform.workorders.enums.WorkOrderPriority;

import java.math.BigDecimal;
import java.time.Instant;

public class RepairRecommendationDto {

    private Long id;
    private String recommendationCode;
    private Long assetId;
    private String assetCode;
    private String assetName;
    private Long inspectionId;
    private Long conditionAssessmentId;
    private RecommendationAction recommendedAction;
    private WorkOrderPriority priority;
    private String title;
    private String justification;
    private String suggestedScopeOfWork;
    private BigDecimal estimatedCost;
    private BigDecimal estimatedDowntimeHours;
    private RecommendationStatus status;
    private Long workOrderId;
    private String workOrderNumber;
    private String reviewedBy;
    private Instant reviewedAt;
    private String reviewNotes;
    private Instant createdAt;

    public RepairRecommendationDto() {}

    public static RepairRecommendationDto fromEntity(RepairRecommendation rec) {
        if (rec == null) return null;
        RepairRecommendationDto dto = new RepairRecommendationDto();
        dto.setId(rec.getId());
        dto.setRecommendationCode(rec.getRecommendationCode());
        if (rec.getAsset() != null) {
            dto.setAssetId(rec.getAsset().getId());
            dto.setAssetCode(rec.getAsset().getAssetCode());
            dto.setAssetName(rec.getAsset().getName());
        }
        if (rec.getInspection() != null) dto.setInspectionId(rec.getInspection().getId());
        if (rec.getConditionAssessment() != null) dto.setConditionAssessmentId(rec.getConditionAssessment().getId());
        dto.setRecommendedAction(rec.getRecommendedAction());
        dto.setPriority(rec.getPriority());
        dto.setTitle(rec.getTitle());
        dto.setJustification(rec.getJustification());
        dto.setSuggestedScopeOfWork(rec.getSuggestedScopeOfWork());
        dto.setEstimatedCost(rec.getEstimatedCost());
        dto.setEstimatedDowntimeHours(rec.getEstimatedDowntimeHours());
        dto.setStatus(rec.getStatus());
        if (rec.getWorkOrder() != null) {
            dto.setWorkOrderId(rec.getWorkOrder().getId());
            dto.setWorkOrderNumber(rec.getWorkOrder().getWorkOrderNumber());
        }
        dto.setReviewedBy(rec.getReviewedBy());
        dto.setReviewedAt(rec.getReviewedAt());
        dto.setReviewNotes(rec.getReviewNotes());
        dto.setCreatedAt(rec.getCreatedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRecommendationCode() { return recommendationCode; }
    public void setRecommendationCode(String recommendationCode) { this.recommendationCode = recommendationCode; }
    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }
    public String getAssetCode() { return assetCode; }
    public void setAssetCode(String assetCode) { this.assetCode = assetCode; }
    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }
    public Long getInspectionId() { return inspectionId; }
    public void setInspectionId(Long inspectionId) { this.inspectionId = inspectionId; }
    public Long getConditionAssessmentId() { return conditionAssessmentId; }
    public void setConditionAssessmentId(Long conditionAssessmentId) { this.conditionAssessmentId = conditionAssessmentId; }
    public RecommendationAction getRecommendedAction() { return recommendedAction; }
    public void setRecommendedAction(RecommendationAction recommendedAction) { this.recommendedAction = recommendedAction; }
    public WorkOrderPriority getPriority() { return priority; }
    public void setPriority(WorkOrderPriority priority) { this.priority = priority; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }
    public String getSuggestedScopeOfWork() { return suggestedScopeOfWork; }
    public void setSuggestedScopeOfWork(String suggestedScopeOfWork) { this.suggestedScopeOfWork = suggestedScopeOfWork; }
    public BigDecimal getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(BigDecimal estimatedCost) { this.estimatedCost = estimatedCost; }
    public BigDecimal getEstimatedDowntimeHours() { return estimatedDowntimeHours; }
    public void setEstimatedDowntimeHours(BigDecimal estimatedDowntimeHours) { this.estimatedDowntimeHours = estimatedDowntimeHours; }
    public RecommendationStatus getStatus() { return status; }
    public void setStatus(RecommendationStatus status) { this.status = status; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public String getWorkOrderNumber() { return workOrderNumber; }
    public void setWorkOrderNumber(String workOrderNumber) { this.workOrderNumber = workOrderNumber; }
    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    public Instant getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }
    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
