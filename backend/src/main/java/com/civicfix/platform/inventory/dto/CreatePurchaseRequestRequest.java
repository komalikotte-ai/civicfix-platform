package com.civicfix.platform.inventory.dto;

import com.civicfix.platform.inventory.enums.PurchasePriority;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CreatePurchaseRequestRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    private String justification;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    private Long warehouseId;
    private Long workOrderId;
    private Long requestedById;

    @NotNull(message = "Priority is required")
    private PurchasePriority priority = PurchasePriority.MEDIUM;

    private LocalDate expectedDelivery;

    @NotNull(message = "Line items are required")
    @Size(min = 1, message = "At least one line item is required")
    @Valid
    private List<PurchaseRequestLineItemRequest> items;

    public CreatePurchaseRequestRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public Long getRequestedById() { return requestedById; }
    public void setRequestedById(Long requestedById) { this.requestedById = requestedById; }
    public PurchasePriority getPriority() { return priority; }
    public void setPriority(PurchasePriority priority) { this.priority = priority; }
    public LocalDate getExpectedDelivery() { return expectedDelivery; }
    public void setExpectedDelivery(LocalDate expectedDelivery) { this.expectedDelivery = expectedDelivery; }
    public List<PurchaseRequestLineItemRequest> getItems() { return items; }
    public void setItems(List<PurchaseRequestLineItemRequest> items) { this.items = items; }
}
