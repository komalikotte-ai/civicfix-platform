package com.civicfix.platform.workorders.dto;

import com.civicfix.platform.workorders.entity.WorkOrder;
import com.civicfix.platform.workorders.enums.WorkOrderPriority;
import com.civicfix.platform.workorders.enums.WorkOrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public class WorkOrderDto {

    private Long id;
    private String workOrderNumber;
    private Long complaintId;
    private String complaintTrackingNumber;
    private Long assetId;
    private String assetCode;
    private Long organizationId;
    private String organizationName;
    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private Long wardId;
    private String wardNumber;
    private String wardName;
    private String title;
    private String description;
    private WorkOrderPriority priority;
    private WorkOrderStatus status;
    private Long assignedWorkerId;
    private String assignedWorkerCode;
    private String assignedWorkerName;
    private Long assignedSupervisorId;
    private String assignedSupervisorCode;
    private String assignedSupervisorName;
    private Instant assignedAt;
    private Instant scheduledStartDate;
    private Instant expectedCompletionDate;
    private Instant actualStartDate;
    private Instant actualCompletionDate;
    private String addressLine;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal estimatedCost;
    private BigDecimal actualCost;
    private String completionNotes;
    private String evidenceMetadata;
    private String rejectionReason;
    private int rejectionCount;
    private Long verifiedById;
    private String verifiedByName;
    private Instant verifiedAt;
    private String verificationNotes;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;

    public WorkOrderDto() {
    }

    public static WorkOrderDto fromEntity(WorkOrder entity) {
        if (entity == null) {
            return null;
        }

        WorkOrderDto dto = new WorkOrderDto();
        dto.setId(entity.getId());
        dto.setWorkOrderNumber(entity.getWorkOrderNumber());

        if (entity.getComplaint() != null) {
            dto.setComplaintId(entity.getComplaint().getId());
            dto.setComplaintTrackingNumber(entity.getComplaint().getTrackingNumber());
        }

        if (entity.getAsset() != null) {
            dto.setAssetId(entity.getAsset().getId());
            dto.setAssetCode(entity.getAsset().getAssetCode());
        }

        if (entity.getOrganization() != null) {
            dto.setOrganizationId(entity.getOrganization().getId());
            dto.setOrganizationName(entity.getOrganization().getName());
        }

        if (entity.getDepartment() != null) {
            dto.setDepartmentId(entity.getDepartment().getId());
            dto.setDepartmentName(entity.getDepartment().getName());
            dto.setDepartmentCode(entity.getDepartment().getCode());
        }

        if (entity.getWard() != null) {
            dto.setWardId(entity.getWard().getId());
            dto.setWardNumber(entity.getWard().getWardNumber());
            dto.setWardName(entity.getWard().getName());
        }

        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setPriority(entity.getPriority());
        dto.setStatus(entity.getStatus());

        if (entity.getAssignedWorker() != null) {
            dto.setAssignedWorkerId(entity.getAssignedWorker().getId());
            dto.setAssignedWorkerCode(entity.getAssignedWorker().getEmployeeCode());
            dto.setAssignedWorkerName(entity.getAssignedWorker().getFullName());
        }

        if (entity.getAssignedSupervisor() != null) {
            dto.setAssignedSupervisorId(entity.getAssignedSupervisor().getId());
            dto.setAssignedSupervisorCode(entity.getAssignedSupervisor().getEmployeeCode());
            dto.setAssignedSupervisorName(entity.getAssignedSupervisor().getFullName());
        }

        dto.setAssignedAt(entity.getAssignedAt());
        dto.setScheduledStartDate(entity.getScheduledStartDate());
        dto.setExpectedCompletionDate(entity.getExpectedCompletionDate());
        dto.setActualStartDate(entity.getActualStartDate());
        dto.setActualCompletionDate(entity.getActualCompletionDate());
        dto.setAddressLine(entity.getAddressLine());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setEstimatedCost(entity.getEstimatedCost());
        dto.setActualCost(entity.getActualCost());
        dto.setCompletionNotes(entity.getCompletionNotes());
        dto.setEvidenceMetadata(entity.getEvidenceMetadata());
        dto.setRejectionReason(entity.getRejectionReason());
        dto.setRejectionCount(entity.getRejectionCount());

        if (entity.getVerifiedBy() != null) {
            dto.setVerifiedById(entity.getVerifiedBy().getId());
            dto.setVerifiedByName(entity.getVerifiedBy().getFullName());
        }

        dto.setVerifiedAt(entity.getVerifiedAt());
        dto.setVerificationNotes(entity.getVerificationNotes());
        dto.setActive(entity.isActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());

        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWorkOrderNumber() {
        return workOrderNumber;
    }

    public void setWorkOrderNumber(String workOrderNumber) {
        this.workOrderNumber = workOrderNumber;
    }

    public Long getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(Long complaintId) {
        this.complaintId = complaintId;
    }

    public String getComplaintTrackingNumber() {
        return complaintTrackingNumber;
    }

    public void setComplaintTrackingNumber(String complaintTrackingNumber) {
        this.complaintTrackingNumber = complaintTrackingNumber;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
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

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
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

    public String getWardName() {
        return wardName;
    }

    public void setWardName(String wardName) {
        this.wardName = wardName;
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

    public WorkOrderPriority getPriority() {
        return priority;
    }

    public void setPriority(WorkOrderPriority priority) {
        this.priority = priority;
    }

    public WorkOrderStatus getStatus() {
        return status;
    }

    public void setStatus(WorkOrderStatus status) {
        this.status = status;
    }

    public Long getAssignedWorkerId() {
        return assignedWorkerId;
    }

    public void setAssignedWorkerId(Long assignedWorkerId) {
        this.assignedWorkerId = assignedWorkerId;
    }

    public String getAssignedWorkerCode() {
        return assignedWorkerCode;
    }

    public void setAssignedWorkerCode(String assignedWorkerCode) {
        this.assignedWorkerCode = assignedWorkerCode;
    }

    public String getAssignedWorkerName() {
        return assignedWorkerName;
    }

    public void setAssignedWorkerName(String assignedWorkerName) {
        this.assignedWorkerName = assignedWorkerName;
    }

    public Long getAssignedSupervisorId() {
        return assignedSupervisorId;
    }

    public void setAssignedSupervisorId(Long assignedSupervisorId) {
        this.assignedSupervisorId = assignedSupervisorId;
    }

    public String getAssignedSupervisorCode() {
        return assignedSupervisorCode;
    }

    public void setAssignedSupervisorCode(String assignedSupervisorCode) {
        this.assignedSupervisorCode = assignedSupervisorCode;
    }

    public String getAssignedSupervisorName() {
        return assignedSupervisorName;
    }

    public void setAssignedSupervisorName(String assignedSupervisorName) {
        this.assignedSupervisorName = assignedSupervisorName;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Instant assignedAt) {
        this.assignedAt = assignedAt;
    }

    public Instant getScheduledStartDate() {
        return scheduledStartDate;
    }

    public void setScheduledStartDate(Instant scheduledStartDate) {
        this.scheduledStartDate = scheduledStartDate;
    }

    public Instant getExpectedCompletionDate() {
        return expectedCompletionDate;
    }

    public void setExpectedCompletionDate(Instant expectedCompletionDate) {
        this.expectedCompletionDate = expectedCompletionDate;
    }

    public Instant getActualStartDate() {
        return actualStartDate;
    }

    public void setActualStartDate(Instant actualStartDate) {
        this.actualStartDate = actualStartDate;
    }

    public Instant getActualCompletionDate() {
        return actualCompletionDate;
    }

    public void setActualCompletionDate(Instant actualCompletionDate) {
        this.actualCompletionDate = actualCompletionDate;
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

    public BigDecimal getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(BigDecimal estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public BigDecimal getActualCost() {
        return actualCost;
    }

    public void setActualCost(BigDecimal actualCost) {
        this.actualCost = actualCost;
    }

    public String getCompletionNotes() {
        return completionNotes;
    }

    public void setCompletionNotes(String completionNotes) {
        this.completionNotes = completionNotes;
    }

    public String getEvidenceMetadata() {
        return evidenceMetadata;
    }

    public void setEvidenceMetadata(String evidenceMetadata) {
        this.evidenceMetadata = evidenceMetadata;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public int getRejectionCount() {
        return rejectionCount;
    }

    public void setRejectionCount(int rejectionCount) {
        this.rejectionCount = rejectionCount;
    }

    public Long getVerifiedById() {
        return verifiedById;
    }

    public void setVerifiedById(Long verifiedById) {
        this.verifiedById = verifiedById;
    }

    public String getVerifiedByName() {
        return verifiedByName;
    }

    public void setVerifiedByName(String verifiedByName) {
        this.verifiedByName = verifiedByName;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public String getVerificationNotes() {
        return verificationNotes;
    }

    public void setVerificationNotes(String verificationNotes) {
        this.verificationNotes = verificationNotes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public String getAssetCode() {
        return assetCode;
    }

    public void setAssetCode(String assetCode) {
        this.assetCode = assetCode;
    }
}
