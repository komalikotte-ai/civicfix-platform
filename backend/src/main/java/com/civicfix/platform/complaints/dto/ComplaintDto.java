package com.civicfix.platform.complaints.dto;

import com.civicfix.platform.complaints.entity.Complaint;
import com.civicfix.platform.complaints.enums.ComplaintPriority;
import com.civicfix.platform.complaints.enums.ComplaintStatus;

import java.math.BigDecimal;
import java.time.Instant;

public class ComplaintDto {

    private Long id;
    private String trackingNumber;
    private Long organizationId;
    private Long wardId;
    private String wardNumber;
    private Long departmentId;
    private String departmentName;
    private Long citizenId;
    private String citizenName;
    private Long assignedOfficerId;
    private String assignedOfficerName;
    private String title;
    private String description;
    private String category;
    private ComplaintPriority priority;
    private ComplaintStatus status;
    private String addressLine;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Instant slaDueAt;
    private Instant resolvedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public ComplaintDto() {
    }

    public static ComplaintDto fromEntity(Complaint entity) {
        ComplaintDto dto = new ComplaintDto();
        dto.setId(entity.getId());
        dto.setTrackingNumber(entity.getTrackingNumber());
        dto.setOrganizationId(entity.getOrganization().getId());
        dto.setWardId(entity.getWard().getId());
        dto.setWardNumber(entity.getWard().getWardNumber());
        if (entity.getDepartment() != null) {
            dto.setDepartmentId(entity.getDepartment().getId());
            dto.setDepartmentName(entity.getDepartment().getName());
        }
        dto.setCitizenId(entity.getCitizen().getId());
        dto.setCitizenName(entity.getCitizen().getFullName());
        if (entity.getAssignedOfficer() != null) {
            dto.setAssignedOfficerId(entity.getAssignedOfficer().getId());
            dto.setAssignedOfficerName(entity.getAssignedOfficer().getFullName());
        }
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setCategory(entity.getCategory());
        dto.setPriority(entity.getPriority());
        dto.setStatus(entity.getStatus());
        dto.setAddressLine(entity.getAddressLine());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setSlaDueAt(entity.getSlaDueAt());
        dto.setResolvedAt(entity.getResolvedAt());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getWardId() {
        return wardId;
    }

    public void setWardId(Long wardId) {
        this.wardId = wardId;
    }

    public String getWardNumber() {
        return wardNumber;
    }

    public void setWardNumber(String wardNumber) {
        this.wardNumber = wardNumber;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Long getCitizenId() {
        return citizenId;
    }

    public void setCitizenId(Long citizenId) {
        this.citizenId = citizenId;
    }

    public String getCitizenName() {
        return citizenName;
    }

    public void setCitizenName(String citizenName) {
        this.citizenName = citizenName;
    }

    public Long getAssignedOfficerId() {
        return assignedOfficerId;
    }

    public void setAssignedOfficerId(Long assignedOfficerId) {
        this.assignedOfficerId = assignedOfficerId;
    }

    public String getAssignedOfficerName() {
        return assignedOfficerName;
    }

    public void setAssignedOfficerName(String assignedOfficerName) {
        this.assignedOfficerName = assignedOfficerName;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
