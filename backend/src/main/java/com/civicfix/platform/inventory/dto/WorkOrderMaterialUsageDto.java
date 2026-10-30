package com.civicfix.platform.inventory.dto;

import com.civicfix.platform.inventory.entity.WorkOrderMaterialUsage;

import java.math.BigDecimal;
import java.time.Instant;

public class WorkOrderMaterialUsageDto {

    private Long id;
    private Long workOrderId;
    private String workOrderNumber;
    private Long itemId;
    private String itemCode;
    private String itemName;
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private int quantityUsed;
    private BigDecimal unitPrice;
    private BigDecimal totalCost;
    private String usageNotes;
    private String recordedBy;
    private Instant recordedAt;
    private Instant createdAt;

    public WorkOrderMaterialUsageDto() {}

    public static WorkOrderMaterialUsageDto fromEntity(WorkOrderMaterialUsage entity) {
        if (entity == null) return null;
        WorkOrderMaterialUsageDto dto = new WorkOrderMaterialUsageDto();
        dto.setId(entity.getId());
        if (entity.getWorkOrder() != null) {
            dto.setWorkOrderId(entity.getWorkOrder().getId());
            dto.setWorkOrderNumber(entity.getWorkOrder().getWorkOrderNumber());
        }
        if (entity.getItem() != null) {
            dto.setItemId(entity.getItem().getId());
            dto.setItemCode(entity.getItem().getItemCode());
            dto.setItemName(entity.getItem().getName());
        }
        if (entity.getWarehouse() != null) {
            dto.setWarehouseId(entity.getWarehouse().getId());
            dto.setWarehouseCode(entity.getWarehouse().getWarehouseCode());
            dto.setWarehouseName(entity.getWarehouse().getName());
        }
        dto.setQuantityUsed(entity.getQuantityUsed());
        dto.setUnitPrice(entity.getUnitPrice());
        dto.setTotalCost(entity.getTotalCost());
        dto.setUsageNotes(entity.getUsageNotes());
        dto.setRecordedBy(entity.getRecordedBy());
        dto.setRecordedAt(entity.getRecordedAt());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public String getWorkOrderNumber() { return workOrderNumber; }
    public void setWorkOrderNumber(String workOrderNumber) { this.workOrderNumber = workOrderNumber; }
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getWarehouseCode() { return warehouseCode; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public int getQuantityUsed() { return quantityUsed; }
    public void setQuantityUsed(int quantityUsed) { this.quantityUsed = quantityUsed; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
    public String getUsageNotes() { return usageNotes; }
    public void setUsageNotes(String usageNotes) { this.usageNotes = usageNotes; }
    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }
    public Instant getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Instant recordedAt) { this.recordedAt = recordedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
