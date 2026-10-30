package com.civicfix.platform.maintenance.entity;

import com.civicfix.platform.assets.entity.Asset;
import com.civicfix.platform.assets.entity.AssetInspection;
import com.civicfix.platform.common.entity.BaseAuditableEntity;
import com.civicfix.platform.maintenance.enums.RecommendationAction;
import com.civicfix.platform.maintenance.enums.RecommendationStatus;
import com.civicfix.platform.workorders.entity.WorkOrder;
import com.civicfix.platform.workorders.enums.WorkOrderPriority;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "repair_recommendations", indexes = {
        @Index(name = "idx_rec_code", columnList = "recommendation_code"),
        @Index(name = "idx_rec_asset", columnList = "asset_id"),
        @Index(name = "idx_rec_status", columnList = "status"),
        @Index(name = "idx_rec_priority", columnList = "priority")
})
public class RepairRecommendation extends BaseAuditableEntity {

    @Column(name = "recommendation_code", nullable = false, unique = true, length = 50)
    private String recommendationCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_id")
    private AssetInspection inspection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condition_assessment_id")
    private ConditionAssessment conditionAssessment;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommended_action", nullable = false, length = 50)
    private RecommendationAction recommendedAction = RecommendationAction.REPAIR;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 30)
    private WorkOrderPriority priority = WorkOrderPriority.MEDIUM;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "justification", nullable = false, columnDefinition = "TEXT")
    private String justification;

    @Column(name = "suggested_scope_of_work", columnDefinition = "TEXT")
    private String suggestedScopeOfWork;

    @Column(name = "estimated_cost", precision = 12, scale = 2)
    private BigDecimal estimatedCost = BigDecimal.ZERO;

    @Column(name = "estimated_downtime_hours", precision = 6, scale = 2)
    private BigDecimal estimatedDowntimeHours = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RecommendationStatus status = RecommendationStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id")
    private WorkOrder workOrder;

    @Column(name = "reviewed_by", length = 100)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;

    public RepairRecommendation() {}

    public String getRecommendationCode() { return recommendationCode; }
    public void setRecommendationCode(String recommendationCode) { this.recommendationCode = recommendationCode; }
    public Asset getAsset() { return asset; }
    public void setAsset(Asset asset) { this.asset = asset; }
    public AssetInspection getInspection() { return inspection; }
    public void setInspection(AssetInspection inspection) { this.inspection = inspection; }
    public ConditionAssessment getConditionAssessment() { return conditionAssessment; }
    public void setConditionAssessment(ConditionAssessment conditionAssessment) { this.conditionAssessment = conditionAssessment; }
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
    public WorkOrder getWorkOrder() { return workOrder; }
    public void setWorkOrder(WorkOrder workOrder) { this.workOrder = workOrder; }
    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    public Instant getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }
    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }
}
