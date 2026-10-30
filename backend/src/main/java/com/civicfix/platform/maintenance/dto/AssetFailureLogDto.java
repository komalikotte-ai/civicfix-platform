package com.civicfix.platform.maintenance.dto;

import com.civicfix.platform.maintenance.entity.AssetFailureLog;
import com.civicfix.platform.maintenance.enums.FailureSeverity;

import java.math.BigDecimal;
import java.time.Instant;

public class AssetFailureLogDto {

    private Long id;
    private String failureCode;
    private Long assetId;
    private String assetCode;
    private String assetName;
    private Instant failureDate;
    private Long reportedById;
    private String reportedByName;
    private String failureCategory;
    private String failureMode;
    private String rootCause;
    private FailureSeverity severity;
    private BigDecimal downtimeHours;
    private String immediateActionTaken;
    private String preventiveActionRecommended;
    private Long workOrderId;
    private String workOrderNumber;
    private Instant createdAt;

    public AssetFailureLogDto() {}

    public static AssetFailureLogDto fromEntity(AssetFailureLog log) {
        if (log == null) return null;
        AssetFailureLogDto dto = new AssetFailureLogDto();
        dto.setId(log.getId());
        dto.setFailureCode(log.getFailureCode());
        if (log.getAsset() != null) {
            dto.setAssetId(log.getAsset().getId());
            dto.setAssetCode(log.getAsset().getAssetCode());
            dto.setAssetName(log.getAsset().getName());
        }
        dto.setFailureDate(log.getFailureDate());
        if (log.getReportedBy() != null) {
            dto.setReportedById(log.getReportedBy().getId());
            dto.setReportedByName(log.getReportedBy().getFullName());
        }
        dto.setFailureCategory(log.getFailureCategory());
        dto.setFailureMode(log.getFailureMode());
        dto.setRootCause(log.getRootCause());
        dto.setSeverity(log.getSeverity());
        dto.setDowntimeHours(log.getDowntimeHours());
        dto.setImmediateActionTaken(log.getImmediateActionTaken());
        dto.setPreventiveActionRecommended(log.getPreventiveActionRecommended());
        if (log.getWorkOrder() != null) {
            dto.setWorkOrderId(log.getWorkOrder().getId());
            dto.setWorkOrderNumber(log.getWorkOrder().getWorkOrderNumber());
        }
        dto.setCreatedAt(log.getCreatedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFailureCode() { return failureCode; }
    public void setFailureCode(String failureCode) { this.failureCode = failureCode; }
    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }
    public String getAssetCode() { return assetCode; }
    public void setAssetCode(String assetCode) { this.assetCode = assetCode; }
    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }
    public Instant getFailureDate() { return failureDate; }
    public void setFailureDate(Instant failureDate) { this.failureDate = failureDate; }
    public Long getReportedById() { return reportedById; }
    public void setReportedById(Long reportedById) { this.reportedById = reportedById; }
    public String getReportedByName() { return reportedByName; }
    public void setReportedByName(String reportedByName) { this.reportedByName = reportedByName; }
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
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public String getWorkOrderNumber() { return workOrderNumber; }
    public void setWorkOrderNumber(String workOrderNumber) { this.workOrderNumber = workOrderNumber; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
