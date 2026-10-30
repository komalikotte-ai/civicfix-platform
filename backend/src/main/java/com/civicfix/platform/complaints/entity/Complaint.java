package com.civicfix.platform.complaints.entity;

import com.civicfix.platform.common.entity.BaseAuditableEntity;
import com.civicfix.platform.complaints.enums.ComplaintPriority;
import com.civicfix.platform.complaints.enums.ComplaintSeverity;
import com.civicfix.platform.complaints.enums.ComplaintSource;
import com.civicfix.platform.complaints.enums.ComplaintStatus;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.organizations.entity.Organization;
import com.civicfix.platform.sla.entity.ComplaintEscalationHistory;
import com.civicfix.platform.sla.enums.EscalationLevel;
import com.civicfix.platform.sla.enums.SlaState;
import com.civicfix.platform.users.entity.Employee;
import com.civicfix.platform.users.entity.User;
import com.civicfix.platform.wards.entity.Ward;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "complaints", indexes = {
        @Index(name = "idx_complaint_status", columnList = "status"),
        @Index(name = "idx_complaint_ward", columnList = "ward_id"),
        @Index(name = "idx_complaint_dept", columnList = "department_id"),
        @Index(name = "idx_complaint_tracking", columnList = "tracking_number"),
        @Index(name = "idx_complaint_citizen", columnList = "citizen_id"),
        @Index(name = "idx_complaint_employee", columnList = "assigned_employee_id"),
        @Index(name = "idx_complaint_dup_group", columnList = "duplicate_group_id")
})
public class Complaint extends BaseAuditableEntity {

    @Column(name = "tracking_number", nullable = false, unique = true, length = 50)
    private String trackingNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id", nullable = false)
    private Ward ward;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private com.civicfix.platform.assets.entity.Asset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id", nullable = false)
    private User citizen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_officer_id")
    private User assignedOfficer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_employee_id")
    private Employee assignedEmployee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_supervisor_id")
    private Employee assignedSupervisor;

    @Column(name = "assigned_at")
    private Instant assignedAt;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private ComplaintPriority priority = ComplaintPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private ComplaintSeverity severity = ComplaintSeverity.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ComplaintStatus status = ComplaintStatus.NEW;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 30)
    private ComplaintSource source = ComplaintSource.CITIZEN_PORTAL;

    @Column(name = "address_line", columnDefinition = "TEXT")
    private String addressLine;

    @Column(name = "landmark", length = 255)
    private String landmark;

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "sla_due_at")
    private Instant slaDueAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "reopened_count", nullable = false)
    private int reopenedCount = 0;

    @Column(name = "last_reopened_at")
    private Instant lastReopenedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "sla_state", nullable = false, length = 30)
    private SlaState slaState = SlaState.NOT_STARTED;

    @Column(name = "sla_warning_at")
    private Instant slaWarningAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "escalation_level", nullable = false, length = 30)
    private EscalationLevel escalationLevel = EscalationLevel.LEVEL_0;

    @Column(name = "last_escalated_at")
    private Instant lastEscalatedAt;

    // AI Duplicate Grouping Fields
    @Column(name = "duplicate_group_id", length = 100)
    private String duplicateGroupId;

    @Column(name = "is_duplicate", nullable = false)
    private boolean duplicate = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "duplicate_of_id")
    private Complaint duplicateOf;

    @Column(name = "duplicate_similarity_score")
    private Double duplicateSimilarityScore;

    @Column(name = "duplicate_confirmed", nullable = false)
    private boolean duplicateConfirmed = false;

    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComplaintAttachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComplaintComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComplaintEscalationHistory> escalationHistories = new ArrayList<>();

    public Complaint() {
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Ward getWard() {
        return ward;
    }

    public void setWard(Ward ward) {
        this.ward = ward;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public User getCitizen() {
        return citizen;
    }

    public void setCitizen(User citizen) {
        this.citizen = citizen;
    }

    public User getAssignedOfficer() {
        return assignedOfficer;
    }

    public void setAssignedOfficer(User assignedOfficer) {
        this.assignedOfficer = assignedOfficer;
    }

    public Employee getAssignedEmployee() {
        return assignedEmployee;
    }

    public void setAssignedEmployee(Employee assignedEmployee) {
        this.assignedEmployee = assignedEmployee;
    }

    public Employee getAssignedSupervisor() {
        return assignedSupervisor;
    }

    public void setAssignedSupervisor(Employee assignedSupervisor) {
        this.assignedSupervisor = assignedSupervisor;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Instant assignedAt) {
        this.assignedAt = assignedAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public ComplaintPriority getPriority() {
        return priority;
    }

    public void setPriority(ComplaintPriority priority) {
        this.priority = priority;
    }

    public ComplaintSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(ComplaintSeverity severity) {
        this.severity = severity;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }

    public ComplaintSource getSource() {
        return source;
    }

    public void setSource(ComplaintSource source) {
        this.source = source;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public Instant getSlaDueAt() {
        return slaDueAt;
    }

    public void setSlaDueAt(Instant slaDueAt) {
        this.slaDueAt = slaDueAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public int getReopenedCount() {
        return reopenedCount;
    }

    public void setReopenedCount(int reopenedCount) {
        this.reopenedCount = reopenedCount;
    }

    public Instant getLastReopenedAt() {
        return lastReopenedAt;
    }

    public void setLastReopenedAt(Instant lastReopenedAt) {
        this.lastReopenedAt = lastReopenedAt;
    }

    public String getDuplicateGroupId() {
        return duplicateGroupId;
    }

    public void setDuplicateGroupId(String duplicateGroupId) {
        this.duplicateGroupId = duplicateGroupId;
    }

    public boolean isDuplicate() {
        return duplicate;
    }

    public void setDuplicate(boolean duplicate) {
        this.duplicate = duplicate;
    }

    public Complaint getDuplicateOf() {
        return duplicateOf;
    }

    public void setDuplicateOf(Complaint duplicateOf) {
        this.duplicateOf = duplicateOf;
    }

    public Double getDuplicateSimilarityScore() {
        return duplicateSimilarityScore;
    }

    public void setDuplicateSimilarityScore(Double duplicateSimilarityScore) {
        this.duplicateSimilarityScore = duplicateSimilarityScore;
    }

    public boolean isDuplicateConfirmed() {
        return duplicateConfirmed;
    }

    public void setDuplicateConfirmed(boolean duplicateConfirmed) {
        this.duplicateConfirmed = duplicateConfirmed;
    }

    public List<ComplaintAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<ComplaintAttachment> attachments) {
        this.attachments = attachments;
    }

    public List<ComplaintComment> getComments() {
        return comments;
    }

    public void setComments(List<ComplaintComment> comments) {
        this.comments = comments;
    }

    public SlaState getSlaState() {
        return slaState;
    }

    public void setSlaState(SlaState slaState) {
        this.slaState = slaState;
    }

    public Instant getSlaWarningAt() {
        return slaWarningAt;
    }

    public void setSlaWarningAt(Instant slaWarningAt) {
        this.slaWarningAt = slaWarningAt;
    }

    public EscalationLevel getEscalationLevel() {
        return escalationLevel;
    }

    public void setEscalationLevel(EscalationLevel escalationLevel) {
        this.escalationLevel = escalationLevel;
    }

    public Instant getLastEscalatedAt() {
        return lastEscalatedAt;
    }

    public void setLastEscalatedAt(Instant lastEscalatedAt) {
        this.lastEscalatedAt = lastEscalatedAt;
    }

    public List<ComplaintEscalationHistory> getEscalationHistories() {
        return escalationHistories;
    }

    public void setEscalationHistories(List<ComplaintEscalationHistory> escalationHistories) {
        this.escalationHistories = escalationHistories;
    }

    public com.civicfix.platform.assets.entity.Asset getAsset() {
        return asset;
    }

    public void setAsset(com.civicfix.platform.assets.entity.Asset asset) {
        this.asset = asset;
    }
}
