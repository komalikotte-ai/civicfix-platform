package com.civicfix.platform.maintenance.dto;

import com.civicfix.platform.maintenance.enums.RecommendationAction;
import com.civicfix.platform.workorders.enums.WorkOrderPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CreateRepairRecommendationRequest {

    private String recommendationCode;

    @NotNull(message = "Asset ID is required")
    private Long assetId;

    private Long inspectionId;
    private Long conditionAssessmentId;

    @NotNull(message = "Recommended action is required")
    private RecommendationAction recommendedAction;

    private WorkOrderPriority priority = WorkOrderPriority.MEDIUM;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Justification is required")
    private String justification;

    private String suggestedScopeOfWork;
    private BigDecimal estimatedCost = BigDecimal.ZERO;
    private BigDecimal estimatedDowntimeHours = BigDecimal.ZERO;

    public CreateRepairRecommendationRequest() {}

    public String getRecommendationCode() { return recommendationCode; }
    public void setRecommendationCode(String recommendationCode) { this.recommendationCode = recommendationCode; }
    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }
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
}
