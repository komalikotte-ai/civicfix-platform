package com.civicfix.platform.complaints.entity;

import com.civicfix.platform.common.entity.BaseAuditableEntity;
import com.civicfix.platform.complaints.enums.ComplaintPriority;
import com.civicfix.platform.complaints.enums.ComplaintStatus;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.organizations.entity.Organization;
import com.civicfix.platform.users.entity.User;
import com.civicfix.platform.wards.entity.Ward;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "complaints", indexes = {
        @Index(name = "idx_complaint_status", columnList = "status"),
        @Index(name = "idx_complaint_ward", columnList = "ward_id"),
        @Index(name = "idx_complaint_dept", columnList = "department_id"),
        @Index(name = "idx_complaint_tracking", columnList = "tracking_number")
})
public class Complaint extends BaseAuditableEntity {

    @Column(name = "tracking_number", nullable = false, unique = true, length = 50)
    private String trackingNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id", nullable = false)
    private Ward ward;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id", nullable = false)
    private User citizen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_officer_id")
    private User assignedOfficer;

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
    @Column(name = "status", nullable = false, length = 30)
    private ComplaintStatus status = ComplaintStatus.SUBMITTED;

    @Column(name = "address_line", columnDefinition = "TEXT")
    private String addressLine;

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "sla_due_at")
    private Instant slaDueAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

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

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
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
}
