package com.civicfix.platform.inventory.dto;

import com.civicfix.platform.inventory.entity.PurchaseRequest;
import com.civicfix.platform.inventory.enums.PurchasePriority;
import com.civicfix.platform.inventory.enums.PurchaseRequestStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class PurchaseRequestDto {

    private Long id;
    private String requestCode;
    private String title;
    private String justification;
    private PurchaseRequestStatus status;
    private PurchasePriority priority;
    private Long departmentId;
    private String departmentName;
    private Long organizationId;
    private String organizationName;
    private Long warehouseId;
    private String warehouseCode;
    private Long requestedById;
    private String requestedByName;
    private String approvedBy;
    private Instant approvedAt;
    private String rejectionReason;
    private LocalDate expectedDelivery;
    private BigDecimal totalEstimatedCost;
    private Long workOrderId;
    private String workOrderNumber;
    private List<PurchaseRequestItemDto> items;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public PurchaseRequestDto() {}

    public static PurchaseRequestDto fromEntity(PurchaseRequest entity) {
        if (entity == null) return null;
        PurchaseRequestDto dto = new PurchaseRequestDto();
        dto.setId(entity.getId());
        dto.setRequestCode(entity.getRequestCode());
        dto.setTitle(entity.getTitle());
        dto.setJustification(entity.getJustification());
        dto.setStatus(entity.getStatus());
        dto.setPriority(entity.getPriority());
        if (entity.getDepartment() != null) {
            dto.setDepartmentId(entity.getDepartment().getId());
            dto.setDepartmentName(entity.getDepartment().getName());
        }
        if (entity.getOrganization() != null) {
            dto.setOrganizationId(entity.getOrganization().getId());
            dto.setOrganizationName(entity.getOrganization().getName());
        }
        if (entity.getWarehouse() != null) {
            dto.setWarehouseId(entity.getWarehouse().getId());
            dto.setWarehouseCode(entity.getWarehouse().getWarehouseCode());
        }
        if (entity.getRequestedBy() != null) {
            dto.setRequestedById(entity.getRequestedBy().getId());
            dto.setRequestedByName(entity.getRequestedBy().getFullName());
        }
        dto.setApprovedBy(entity.getApprovedBy());
        dto.setApprovedAt(entity.getApprovedAt());
        dto.setRejectionReason(entity.getRejectionReason());
        dto.setExpectedDelivery(entity.getExpectedDelivery());
        dto.setTotalEstimatedCost(entity.getTotalEstimatedCost());
        if (entity.getWorkOrder() != null) {
            dto.setWorkOrderId(entity.getWorkOrder().getId());
            dto.setWorkOrderNumber(entity.getWorkOrder().getWorkOrderNumber());
        }
        if (entity.getItems() != null) {
            dto.setItems(entity.getItems().stream()
                    .map(PurchaseRequestItemDto::fromEntity)
                    .collect(Collectors.toList()));
        }
        dto.setActive(entity.isActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRequestCode() { return requestCode; }
    public void setRequestCode(String requestCode) { this.requestCode = requestCode; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }
    public PurchaseRequestStatus getStatus() { return status; }
    public void setStatus(PurchaseRequestStatus status) { this.status = status; }
    public PurchasePriority getPriority() { return priority; }
    public void setPriority(PurchasePriority priority) { this.priority = priority; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }
    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getWarehouseCode() { return warehouseCode; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }
    public Long getRequestedById() { return requestedById; }
    public void setRequestedById(Long requestedById) { this.requestedById = requestedById; }
    public String getRequestedByName() { return requestedByName; }
    public void setRequestedByName(String requestedByName) { this.requestedByName = requestedByName; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public LocalDate getExpectedDelivery() { return expectedDelivery; }
    public void setExpectedDelivery(LocalDate expectedDelivery) { this.expectedDelivery = expectedDelivery; }
    public BigDecimal getTotalEstimatedCost() { return totalEstimatedCost; }
    public void setTotalEstimatedCost(BigDecimal totalEstimatedCost) { this.totalEstimatedCost = totalEstimatedCost; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public String getWorkOrderNumber() { return workOrderNumber; }
    public void setWorkOrderNumber(String workOrderNumber) { this.workOrderNumber = workOrderNumber; }
    public List<PurchaseRequestItemDto> getItems() { return items; }
    public void setItems(List<PurchaseRequestItemDto> items) { this.items = items; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
