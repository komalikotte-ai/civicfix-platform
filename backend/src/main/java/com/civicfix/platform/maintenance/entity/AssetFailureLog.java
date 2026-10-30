package com.civicfix.platform.maintenance.entity;

import com.civicfix.platform.assets.entity.Asset;
import com.civicfix.platform.common.entity.BaseAuditableEntity;
import com.civicfix.platform.maintenance.enums.FailureSeverity;
import com.civicfix.platform.users.entity.Employee;
import com.civicfix.platform.workorders.entity.WorkOrder;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "asset_failure_logs", indexes = {
        @Index(name = "idx_fail_code", columnList = "failure_code"),
        @Index(name = "idx_fail_asset", columnList = "asset_id"),
        @Index(name = "idx_fail_date", columnList = "failure_date"),
        @Index(name = "idx_fail_severity", columnList = "severity")
})
public class AssetFailureLog extends BaseAuditableEntity {

    @Column(name = "failure_code", nullable = false, unique = true, length = 50)
    private String failureCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(name = "failure_date", nullable = false)
    private Instant failureDate = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by_id")
    private Employee reportedBy;

    @Column(name = "failure_category", nullable = false, length = 100)
    private String failureCategory;

    @Column(name = "failure_mode", nullable = false, length = 100)
    private String failureMode;

    @Column(name = "root_cause", nullable = false, columnDefinition = "TEXT")
    private String rootCause;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 30)
    private FailureSeverity severity = FailureSeverity.MODERATE;

    @Column(name = "downtime_hours", precision = 8, scale = 2)
    private BigDecimal downtimeHours = BigDecimal.ZERO;

    @Column(name = "immediate_action_taken", columnDefinition = "TEXT")
    private String immediateActionTaken;

    @Column(name = "preventive_action_recommended", columnDefinition = "TEXT")
    private String preventiveActionRecommended;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id")
    private WorkOrder workOrder;

    public AssetFailureLog() {}

    public String getFailureCode() { return failureCode; }
    public void setFailureCode(String failureCode) { this.failureCode = failureCode; }
    public Asset getAsset() { return asset; }
    public void setAsset(Asset asset) { this.asset = asset; }
    public Instant getFailureDate() { return failureDate; }
    public void setFailureDate(Instant failureDate) { this.failureDate = failureDate; }
    public Employee getReportedBy() { return reportedBy; }
    public void setReportedBy(Employee reportedBy) { this.reportedBy = reportedBy; }
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
    public WorkOrder getWorkOrder() { return workOrder; }
    public void setWorkOrder(WorkOrder workOrder) { this.workOrder = workOrder; }
}
