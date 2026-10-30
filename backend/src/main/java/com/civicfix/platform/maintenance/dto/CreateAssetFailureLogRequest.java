package com.civicfix.platform.maintenance.dto;

import com.civicfix.platform.maintenance.enums.FailureSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CreateAssetFailureLogRequest {

    private String failureCode;

    @NotNull(message = "Asset ID is required")
    private Long assetId;

    private Long reportedById;

    @NotBlank(message = "Failure category is required")
    private String failureCategory;

    @NotBlank(message = "Failure mode is required")
    private String failureMode;

    @NotBlank(message = "Root cause is required")
    private String rootCause;

    private FailureSeverity severity = FailureSeverity.MODERATE;
    private BigDecimal downtimeHours = BigDecimal.ZERO;
    private String immediateActionTaken;
    private String preventiveActionRecommended;
    private boolean createWorkOrder = false;

    public CreateAssetFailureLogRequest() {}

    public String getFailureCode() { return failureCode; }
    public void setFailureCode(String failureCode) { this.failureCode = failureCode; }
    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }
    public Long getReportedById() { return reportedById; }
    public void setReportedById(Long reportedById) { this.reportedById = reportedById; }
    public String getFailureCategory() { return failureCategory; }
    public void setFailureCategory(String failureCategory) { this.failureCategory = failureCategory; }
    public String getFailureMode() { return failureMode; }
    public void setFailureMode(String failureMode) { this.failureMode = failureMode; }
    public String getRootCause() { return rootCause; }
    public void setRootCause(String rootCause) { this.rootCause = rootCause; }
    public FailureSeverity getSeverity() { return severity; }
    public void setSeverity(FailureSeverity severity) { this.severity = severity; }
    public BigDecimal getDowntimeHours() { return downtimeHours; }
    public void setDowntimeHours(BigDecimal downtimeHours) { this.downtimeHours = downtimeHours; }
    public String getImmediateActionTaken() { return immediateActionTaken; }
    public void setImmediateActionTaken(String immediateActionTaken) { this.immediateActionTaken = immediateActionTaken; }
    public String getPreventiveActionRecommended() { return preventiveActionRecommended; }
    public void setPreventiveActionRecommended(String preventiveActionRecommended) { this.preventiveActionRecommended = preventiveActionRecommended; }
    public boolean isCreateWorkOrder() { return createWorkOrder; }
    public void setCreateWorkOrder(boolean createWorkOrder) { this.createWorkOrder = createWorkOrder; }
}
