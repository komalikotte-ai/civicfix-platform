package com.civicfix.platform.complaints.dto;

import com.civicfix.platform.complaints.enums.ComplaintPriority;
import com.civicfix.platform.complaints.enums.ComplaintSeverity;
import com.civicfix.platform.complaints.enums.ComplaintSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CreateComplaintRequest {

    @NotBlank(message = "Complaint title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @NotBlank(message = "Complaint description is required")
    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Ward ID is required")
    private Long wardId;

    private Long departmentId;

    private ComplaintPriority priority = ComplaintPriority.MEDIUM;

    private ComplaintSeverity severity = ComplaintSeverity.MEDIUM;

    private ComplaintSource source = ComplaintSource.CITIZEN_PORTAL;

    private String addressLine;

    @Size(max = 255, message = "Landmark cannot exceed 255 characters")
    private String landmark;

    private BigDecimal latitude;
    private BigDecimal longitude;

    private List<AttachmentItem> attachments = new ArrayList<>();

    public CreateComplaintRequest() {
    }

    public static class AttachmentItem {
        private String fileName;
        private String fileUrl;
        private String fileType;
        private Long fileSize;

        public AttachmentItem() {
        }

        public AttachmentItem(String fileName, String fileUrl, String fileType, Long fileSize) {
            this.fileName = fileName;
            this.fileUrl = fileUrl;
            this.fileType = fileType;
            this.fileSize = fileSize;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFileUrl() {
            return fileUrl;
        }

        public void setFileUrl(String fileUrl) {
            this.fileUrl = fileUrl;
        }

        public String getFileType() {
            return fileType;
        }

        public void setFileType(String fileType) {
            this.fileType = fileType;
        }

        public Long getFileSize() {
            return fileSize;
        }

        public void setFileSize(Long fileSize) {
            this.fileSize = fileSize;
        }
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

    public Long getWardId() {
        return wardId;
    }

    public void setWardId(Long wardId) {
        this.wardId = wardId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
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

    public List<AttachmentItem> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentItem> attachments) {
        this.attachments = attachments;
    }
}
