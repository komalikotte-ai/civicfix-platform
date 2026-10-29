package com.civicfix.platform.complaints.dto;

import com.civicfix.platform.complaints.entity.Complaint;
import com.civicfix.platform.complaints.enums.ComplaintPriority;
import com.civicfix.platform.complaints.enums.ComplaintSeverity;
import com.civicfix.platform.complaints.enums.ComplaintSource;
import com.civicfix.platform.complaints.enums.ComplaintStatus;
import com.civicfix.platform.sla.enums.EscalationLevel;
import com.civicfix.platform.sla.enums.SlaState;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ComplaintDto {

    private Long id;
    private String trackingNumber;
    private Long organizationId;
    private Long wardId;
    private String wardNumber;
    private String wardName;
    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private Long citizenId;
    private String citizenName;
    private String citizenEmail;

    private Long assignedOfficerId;
    private String assignedOfficerName;
    private Long assignedEmployeeId;
    private String assignedEmployeeName;
    private String assignedEmployeeCode;
    private Long assignedSupervisorId;
    private String assignedSupervisorName;
    private Instant assignedAt;

    private String title;
    private String description;
    private String category;
    private ComplaintPriority priority;
    private ComplaintSeverity severity;
    private ComplaintStatus status;
    private ComplaintSource source;
    private String addressLine;
    private String landmark;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Instant slaDueAt;
    private Instant resolvedAt;
    private int reopenedCount;
    private Instant lastReopenedAt;
    private SlaState slaState;
    private Instant slaWarningAt;
    private EscalationLevel escalationLevel;
    private Instant lastEscalatedAt;

    private String duplicateGroupId;
    private boolean duplicate;
    private Long duplicateOfId;
    private String duplicateOfTrackingNumber;
    private Double duplicateSimilarityScore;
    private boolean duplicateConfirmed;

    private List<ComplaintAttachmentDto> attachments = new ArrayList<>();
    private List<ComplaintCommentDto> comments = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;

    public ComplaintDto() {
    }

    public static ComplaintDto fromEntity(Complaint entity) {
        if (entity == null) return null;
        ComplaintDto dto = new ComplaintDto();
        dto.setId(entity.getId());
        dto.setTrackingNumber(entity.getTrackingNumber());
        if (entity.getOrganization() != null) {
            dto.setOrganizationId(entity.getOrganization().getId());
        }
        if (entity.getWard() != null) {
            dto.setWardId(entity.getWard().getId());
            dto.setWardNumber(entity.getWard().getWardNumber());
            dto.setWardName(entity.getWard().getName());
        }
        if (entity.getDepartment() != null) {
            dto.setDepartmentId(entity.getDepartment().getId());
            dto.setDepartmentName(entity.getDepartment().getName());
            dto.setDepartmentCode(entity.getDepartment().getCode());
        }
        if (entity.getCitizen() != null) {
            dto.setCitizenId(entity.getCitizen().getId());
            dto.setCitizenName(entity.getCitizen().getFullName());
            dto.setCitizenEmail(entity.getCitizen().getEmail());
        }
        if (entity.getAssignedOfficer() != null) {
            dto.setAssignedOfficerId(entity.getAssignedOfficer().getId());
            dto.setAssignedOfficerName(entity.getAssignedOfficer().getFullName());
        }
        if (entity.getAssignedEmployee() != null) {
            dto.setAssignedEmployeeId(entity.getAssignedEmployee().getId());
            dto.setAssignedEmployeeName(entity.getAssignedEmployee().getFullName());
            dto.setAssignedEmployeeCode(entity.getAssignedEmployee().getEmployeeCode());
        }
        if (entity.getAssignedSupervisor() != null) {
            dto.setAssignedSupervisorId(entity.getAssignedSupervisor().getId());
            dto.setAssignedSupervisorName(entity.getAssignedSupervisor().getFullName());
        }
        dto.setAssignedAt(entity.getAssignedAt());

        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setCategory(entity.getCategory());
        dto.setPriority(entity.getPriority());
        dto.setSeverity(entity.getSeverity());
        dto.setStatus(entity.getStatus());
        dto.setSource(entity.getSource());
        dto.setAddressLine(entity.getAddressLine());
        dto.setLandmark(entity.getLandmark());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setSlaDueAt(entity.getSlaDueAt());
        dto.setResolvedAt(entity.getResolvedAt());
        dto.setReopenedCount(entity.getReopenedCount());
        dto.setLastReopenedAt(entity.getLastReopenedAt());
        dto.setSlaState(entity.getSlaState());
        dto.setSlaWarningAt(entity.getSlaWarningAt());
        dto.setEscalationLevel(entity.getEscalationLevel());
        dto.setLastEscalatedAt(entity.getLastEscalatedAt());

        dto.setDuplicateGroupId(entity.getDuplicateGroupId());
        dto.setDuplicate(entity.isDuplicate());
        if (entity.getDuplicateOf() != null) {
            dto.setDuplicateOfId(entity.getDuplicateOf().getId());
            dto.setDuplicateOfTrackingNumber(entity.getDuplicateOf().getTrackingNumber());
        }
        dto.setDuplicateSimilarityScore(entity.getDuplicateSimilarityScore());
        dto.setDuplicateConfirmed(entity.isDuplicateConfirmed());

        if (entity.getAttachments() != null) {
            dto.setAttachments(entity.getAttachments().stream()
                    .map(ComplaintAttachmentDto::fromEntity)
                    .collect(Collectors.toList()));
        }
        if (entity.getComments() != null) {
            dto.setComments(entity.getComments().stream()
                    .map(ComplaintCommentDto::fromEntity)
                    .collect(Collectors.toList()));
        }

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

    public String getWardName() {
        return wardName;
    }

    public void setWardName(String wardName) {
        this.wardName = wardName;
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

    public String getCitizenEmail() {
        return citizenEmail;
    }

    public void setCitizenEmail(String citizenEmail) {
        this.citizenEmail = citizenEmail;
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

    public Long getAssignedEmployeeId() {
        return assignedEmployeeId;
    }

    public void setAssignedEmployeeId(Long assignedEmployeeId) {
        this.assignedEmployeeId = assignedEmployeeId;
    }

    public String getAssignedEmployeeName() {
        return assignedEmployeeName;
    }

    public void setAssignedEmployeeName(String assignedEmployeeName) {
        this.assignedEmployeeName = assignedEmployeeName;
    }

    public String getAssignedEmployeeCode() {
        return assignedEmployeeCode;
    }

    public void setAssignedEmployeeCode(String assignedEmployeeCode) {
        this.assignedEmployeeCode = assignedEmployeeCode;
    }

    public Long getAssignedSupervisorId() {
        return assignedSupervisorId;
    }

    public void setAssignedSupervisorId(Long assignedSupervisorId) {
        this.assignedSupervisorId = assignedSupervisorId;
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

    public Long getDuplicateOfId() {
        return duplicateOfId;
    }

    public void setDuplicateOfId(Long duplicateOfId) {
        this.duplicateOfId = duplicateOfId;
    }

    public String getDuplicateOfTrackingNumber() {
        return duplicateOfTrackingNumber;
    }

    public void setDuplicateOfTrackingNumber(String duplicateOfTrackingNumber) {
        this.duplicateOfTrackingNumber = duplicateOfTrackingNumber;
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

    public List<ComplaintAttachmentDto> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<ComplaintAttachmentDto> attachments) {
        this.attachments = attachments;
    }

    public List<ComplaintCommentDto> getComments() {
        return comments;
    }

    public void setComments(List<ComplaintCommentDto> comments) {
        this.comments = comments;
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
}
