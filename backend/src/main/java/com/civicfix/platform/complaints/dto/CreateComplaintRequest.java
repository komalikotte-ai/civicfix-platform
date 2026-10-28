package com.civicfix.platform.complaints.dto;

import com.civicfix.platform.complaints.enums.ComplaintPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class CreateComplaintRequest {

    @NotNull(message = "Ward ID is required")
    private Long wardId;

    private Long departmentId;

    @NotBlank(message = "Complaint title is required")
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    private String title;

    @NotBlank(message = "Complaint description is required")
    @Size(min = 10, message = "Description must be at least 10 characters")
    private String description;

    @NotBlank(message = "Complaint category is required")
    private String category;

    private ComplaintPriority priority = ComplaintPriority.MEDIUM;
    private String addressLine;
    private BigDecimal latitude;
    private BigDecimal longitude;

    public CreateComplaintRequest() {
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
}
